	/*
	 * Copyright (c) 2019, SkylerPIlot <https://github.com/SkylerPIlot>
	 * All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 * 1. Redistributions of source code must retain the above copyright notice, this
	 *    list of conditions and the following disclaimer.
	 * 2. Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */
package com.queuehelper;


import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.io.IOException;
@Slf4j
@PluginDescriptor(name = "BAS Queue Helper", description = "BAS Customer CC Info", tags = {"minigame"})
public class BASPlugin extends Plugin implements ActionListener {
	private static final Logger log = LoggerFactory.getLogger(BASPlugin.class);

	private static final String ccName = "Ba Services";

	private static final String errorMsg = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("BAS QH: ").append(ChatColorType.HIGHLIGHT).append("Please Paste the API key in the plugin settings and restart the plugin").build();

	private BasQueuePanel basQueuePanel;
	private NavigationButton navButton;

	public float fontSize;

	private boolean msgIn = false;

	private static final String KICK_OPTION = "Kick";
	private static final ImmutableList<String> BEFORE_OPTIONS = ImmutableList.of("Add friend", "Remove friend", KICK_OPTION);
	private static final ImmutableList<String> AFTER_OPTIONS = ImmutableList.of("Message");

	private BASHTTPClient httpclient;

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private BASConfig config;

	@Inject
	private OkHttpClient BasHttpClient;

	@Inject
	private ClientToolbar clientToolbar;

	public BASPlugin() throws IOException {
	}

	@Provides
	BASConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BASConfig.class);
	}

	private Queue queue;

	protected void startUp() throws Exception {
		if (!isConfigApiEmpty()) {
			this.basQueuePanel = new BasQueuePanel(this, this.config);
			this.queue = Queue.getInstance(config.apikey(), basQueuePanel, this, BasHttpClient);
			BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panellogo.png");
			navButton = NavigationButton.builder()
					.tooltip("BAS queue + options")
					.icon(icon)
					.priority(2)
					.panel(basQueuePanel)
					.build();
			clientToolbar.addNavigation(navButton);
			SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
			this.fontSize = config.fontSize();
		}
	}

	private int inGameBit = 0;
	private String currentWave = START_WAVE;

	@Getter
	private Round currentRound;



	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
		this.queue = null;
		httpclient = null;
		gameTime = null;
		currentWave = START_WAVE;
		inGameBit = 0;
	}


	//checks on startup of plugin and throws an error
	private boolean isConfigApiEmpty() {

		if (config.apikey().equals("Paste your key here") || config.apikey().equals("")) {

			BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(errorMsg)
					.build());
			client.getAccountHash();
			return true;

		}
		return false;

	}


	@Subscribe
	public void onConfigChanged(ConfigChanged event) throws IOException {
		this.fontSize = config.fontSize();
	}

	@Subscribe
	public void onFriendsChatMemberJoined(FriendsChatMemberJoined event) throws IOException {
		this.queue.ShouldUpdate(true);
	}


	@Subscribe
	public void onFriendsChatMemberLeft(FriendsChatMemberLeft event) throws IOException {
		this.queue.ShouldUpdate(true);
	}

	//used in sending discord webhook messages
	private boolean isRank() {
		try {
			FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
			return this.client.getLocalPlayer().getName() != null && clanMemberManager != null && clanMemberManager.getCount() >= 1 && clanMemberManager.getOwner().equals(ccName);
		} catch (Exception e) {
			return false;
		}

	}

	//builds a stringbuilder that is then passed to the Implementation of BASHTTPClient to call the backend
	public void updateQueue() throws IOException {
		if (!isRank()) {
			return;
		}
		FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
		if (!this.config.autoUpdateQueue() || clanMemberManager == null)
			return;
		StringBuilder csv = new StringBuilder();
		for (FriendsChatMember member : (FriendsChatMember[]) clanMemberManager.getMembers()) {
			String memberName = member.getName();
			if (csv.toString().equals("")) {
				csv = new StringBuilder(memberName);
			} else {
				csv.append(",").append(memberName);
			}
		}
		if (csv.toString().equals(""))
			return;
		if (isConfigApiEmpty()) {
			return;
		}
		String name = config.queueName();
		if (client.getLocalPlayer().getName() != null) {
			name = Text.sanitize(client.getLocalPlayer().getName());
		}
		//System.out.print(csv.toString() + '\n');
		queue.updateQueuebackend(csv, name);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {

		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE
				&& chatMessage.getMessage().startsWith("---- Wave:"))
		{
			String[] message = chatMessage.getMessage().split(" ");
			currentWave = message[BA_WAVE_NUM_INDEX];

			if (currentWave.equals(START_WAVE))
			{
				gameTime = new GameTimer();
				pointsHealer = pointsDefender = pointsCollector = pointsAttacker = totalEggsCollected = totalIncorrectAttacks = totalHealthReplenished = 0;
			}

		}


		if (!isRank() || chatMessage.getType() != ChatMessageType.FRIENDSCHAT) {
			return;
		}

		FriendsChatRank rank = getRank(chatMessage.getName());
		if (isConfigApiEmpty()) {
			return;
		}//TODO fix hanging

		try {
			int numMsg = (int) chatMessage.getMessage().charAt(0);
			if ((48 <= numMsg && numMsg <= 53) && (chatMessage.getMessage().contains("out") || chatMessage.getMessage().contains("f") || chatMessage.getMessage().contains("a") || chatMessage.getMessage().contains("*") || chatMessage.getMessage().contains("c") || chatMessage.getMessage().contains("d") || chatMessage.getMessage().contains("h") || (chatMessage.getMessage().contains("r") && !chatMessage.getMessage().contains("reg")))) {
				if (48 <= ((int) chatMessage.getMessage().charAt(1)) && ((int) chatMessage.getMessage().charAt(1)) <= 57) {
					msgIn = false;
				} else {
					msgIn = true;
				}
			}
		} catch (NumberFormatException ex) {
			log.debug("Normal behavior");
		}


		try {
			int numMsg = Integer.parseInt(chatMessage.getMessage());
			if (0 <= numMsg && numMsg <= 5) {
				msgIn = true;
			}
		} catch (NumberFormatException ex) {
			log.debug("Normal behavior");
		}

		if (chatMessage.getMessage().toLowerCase().contains("t+") || chatMessage.getMessage().toLowerCase().contains("-=-=") || chatMessage.getMessage().toLowerCase().contains("---") || chatMessage.getMessage().toLowerCase().contains("===") || chatMessage.getMessage().toLowerCase().equals("jf") || chatMessage.getMessage().toLowerCase().equals("out")) {
			msgIn = true;
		}


		if (((chatMessage.getMessage().contains("+") && chatMessage.getMessage().charAt(0) == '+') || msgIn) && !chatMessage.getMessage().toLowerCase().contains("@")) {
			msgIn = false;
			queue.sendChatMsgDiscord(chatMessage);


		}
		//TODO implement webhook + blairm messages + retrieving and using reuls from the AWS server
	}

	private FriendsChatRank getRank(String playerName) {
		FriendsChatManager friendsChatManager = this.client.getFriendsChatManager();
		if (friendsChatManager == null)
			return FriendsChatRank.UNRANKED;
		FriendsChatMember friendsChatMember = (FriendsChatMember) friendsChatManager.findByName(playerName);
		return (friendsChatMember != null) ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		//required, didn't feel like using instead used specific functions
	}

	//used in BasQueueRow to run the "Next" button
	public void getNext() {
		Customer next = queue.getNext();
		if (next == null) {
			sendChat("queue empty");
			return;
		}
		sendChat("Next customer in line: Priority " + next.getPriority() + " " + next.getName() + " " + next.getItem() + " " + next.getNotes());
	}

	public void sendChat(String msg) {
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append(msg).build();
		BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build());
	}

	//Adds a customer to the queue and is called by the addcustomer button in the basQueuePanel
	public void addToQueue(String name, String item, String priority) {
		if (name.equals("Customer")) {
			sendChat("Please enter a name");
			return;
		}
		if (queue.addToQueue(item, priority, name, config.queueName())) {
			sendChat("Added: " + name + " for " + priority + " " + item);

		} else {
			sendChat("Failed to add: " + name + " for " + priority + " " + item);
		}
		refreshQueue();

	}

	//used in BasQueueRow to run the "Refresh" button
	public void refreshQueue() {
		//creates a list of online nonranks to update a autocomplete function
		if (client.getGameState() == GameState.LOGGED_IN) {
			FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
			FriendsChatMember[] memberlist = clanMemberManager.getMembers();
			ArrayList<String> keywords = new ArrayList<>();
			for (FriendsChatMember member : memberlist) {
				if (member.getRank() == FriendsChatRank.UNRANKED && !queue.doesCustExist(member.getName())) {
					keywords.add(member.getName());
				}
			}
			basQueuePanel.setAutoCompleteKeyWords(keywords);
		}
		try {
			queue.refresh();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}

	//used in BasQueueRow to run the right click options
	public void markCustomer(int option, Customer cust)
	{
		System.out.print("Attempted to mark: " + cust.getName()+"\n");
		int UNSUPORRTED = 0;
		if(option != UNSUPORRTED)
		{
			try
			{
				queue.mark(option, cust, client.getLocalPlayer().getName());
			}
			catch (IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
		this.refreshQueue();
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}


	//this is where the fun stuff happens!

	private GameTimer gameTime;
	private static final int BA_WAVE_NUM_INDEX = 2;
	private static final String START_WAVE = "1";
	private static final String ENDGAME_REWARD_NEEDLE_TEXT = "<br>5";
	private String round_role;
	private Boolean scanning = false;
	private int round_roleID;
	private Boolean leech;
	//defines all of my specific widgets and icon names could I do it better yes, but like it works
	private Integer BaRoleWidget = 256;
	private Integer BaScrollWidget = 159;
	private Integer leaderID = 8;
	private Integer player1ID = 9;
	private Integer player2ID = 10;
	private Integer player3ID = 11;
	private Integer player4ID = 12;
	private Integer leadericonID = 18;
	private Integer player1iconID = 19;
	private Integer player2iconID = 20;
	private Integer player3iconID = 21;
	private Integer player4iconID = 22;
	private Integer attackerIcon = 20561;
	private Integer defenderIcon = 20566;
	private Integer collectorIcon = 20563;
	private Integer healerIcon = 20569;

	Widget leader;
	Widget leaderIcon;
	Widget player1;
	Widget player1Icon;
	Widget player2;
	Widget player2Icon;
	Widget player3;
	Widget player3Icon;
	Widget player4;
	Widget player4Icon;



	private int pointsHealer, pointsDefender , pointsCollector, pointsAttacker, totalEggsCollected, totalIncorrectAttacks, totalHealthReplenished;
	final int[] childIDsOfPointsWidgets = new int[]{33, 32, 25, 26, 24, 28, 31, 27, 29, 30, 21, 22, 19};

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) throws IOException//exception required to run .flush()
	{
		switch (event.getGroupId())
		{
			case WidgetID.BA_TEAM_GROUP_ID: {
				scanning = true;
				leech = false;
			}
			case 159: {//this is to set scanning true when scroll is used on someone
				scanning = true;
			}
			case 158: {//this is to set scanning true when scroll is used on someone
				scanning = true;
			}
			case WidgetID.BA_REWARD_GROUP_ID:
			{
				Widget rewardWidget = client.getWidget(WidgetInfo.BA_REWARD_TEXT);
				Widget pointsWidget = client.getWidget(WidgetID.BA_REWARD_GROUP_ID, 14); //RUNNERS_PASSED

				// Wave 10 ended
				if (rewardWidget != null && rewardWidget.getText().contains(ENDGAME_REWARD_NEEDLE_TEXT) && gameTime != null)
				{
					ChatMessageBuilder message = new ChatMessageBuilder()
							.append("Attacker: ")
							.append(Color.red, pointsAttacker + 80 + "")
							.append(" |  Healer: ")
							.append(Color.GREEN, pointsHealer + 80 + "")
							.append(" | Defender: ")
							.append(Color.blue, pointsDefender + 80 + "")
							.append(" | Collector: ")
							.append(Color.yellow, pointsCollector + 80 + "")
							.append(System.getProperty("line.separator"))
							.append(totalEggsCollected + " eggs collected, " + totalHealthReplenished + "HP vialed and " + totalIncorrectAttacks + " wrong attacks.");

					String leechRole = IDfinder(player3.getModelId());
					int finalPointsAttacker = pointsAttacker + 80;
					int finalPointsHealer = pointsHealer + 80;
					int finalPointsDefender = pointsDefender + 80;
					int finalPointsCollector = pointsCollector + 80;
					System.out.print("Total Points attacker this wave: "+ finalPointsAttacker+"\n");
					System.out.print("Total Points healer this wave: "+ finalPointsHealer+"\n");
					System.out.print("Total Points defender this wave: "+ finalPointsDefender+"\n");
					System.out.print("Total Points coll this wave: "+ finalPointsCollector+"\n");
					System.out.print("game time:"+gameTime.getPBTime()+"\n");
					System.out.print("Total HP rep coll this wave: "+ totalHealthReplenished+"\n");
					System.out.print("Total Points lost as att this wave: "+ totalIncorrectAttacks+"\n");
					System.out.print("Total eggs coll picked up this wave: "+ totalEggsCollected+"\n");

					System.out.print("Healer identified as: "+ totalEggsCollected+"\n");
					System.out.print("Defender identified as: "+ totalEggsCollected+"\n");
					System.out.print("Attacker identified as: "+ totalEggsCollected+"\n");
					System.out.print("Collecter identified as: "+ totalEggsCollected+"\n");

					//TODO please don't forget to change this back to actually check instead of just true (leech && isRank())
					if(true) {
						if (queue.doesCustExist(player3.getText())) {
							this.queue.sendRoundMsd(leader.getText(), player1.getText(), player2.getText(), player3.getText(),
									player4.getText(), gameTime.getPBTime(), queue.getCustomer(player3.getText()).getPriority(), queue.getCustomer(player3.getText()).getItem()
							,finalPointsAttacker, finalPointsDefender, finalPointsHealer, finalPointsCollector, totalEggsCollected, totalHealthReplenished, totalIncorrectAttacks,leechRole);

						} else {
							this.queue.sendRoundMsd(leader.getText(), player1.getText(), player2.getText(), player3.getText(),
									player4.getText(), gameTime.getPBTime(), "Unknown", "Unknown"
									,finalPointsAttacker, finalPointsDefender, finalPointsHealer, finalPointsCollector, totalEggsCollected, totalHealthReplenished, totalIncorrectAttacks,leechRole);

						}
						gameTime = null;
						leech = false;
					}




				}

				// Wave 1-9 ended
				else if (pointsWidget != null && client.getVar(Varbits.IN_GAME_BA) == 0)
				{
					int wavePoints_Attacker, wavePoints_Defender, wavePoints_Healer, wavePoints_Collector, waveEggsCollected, waveHPReplenished, waveFailedAttacks;

					wavePoints_Attacker = wavePoints_Defender = wavePoints_Healer = wavePoints_Collector = Integer.parseInt(client.getWidget(WidgetID.BA_REWARD_GROUP_ID, childIDsOfPointsWidgets[0]).getText()); //set base pts to all roles
					waveEggsCollected = waveHPReplenished = waveFailedAttacks = 0;

					// Gather post-wave info from points widget
					for (int i = 0; i < childIDsOfPointsWidgets.length; i++)
					{
						int value = Integer.parseInt(client.getWidget(WidgetID.BA_REWARD_GROUP_ID, childIDsOfPointsWidgets[i]).getText());

						switch (i)
						{
							case 1:
							case 2:
							case 3:
								wavePoints_Attacker += value;
								break;
							case 4:
							case 5:
								wavePoints_Defender += value;
								break;
							case 6:
								wavePoints_Collector += value;
								break;
							case 7:
							case 8:
							case 9:
								wavePoints_Healer += value;
								break;
							case 10:
								waveEggsCollected = value;
								totalEggsCollected += value;

								break;
							case 11:
								waveFailedAttacks = value;
								totalIncorrectAttacks += value;
								break;
							case 12:
								waveHPReplenished = value;
								totalHealthReplenished += value;
								break;
						}
					}

					pointsCollector += wavePoints_Collector;
					pointsHealer += wavePoints_Healer;
					pointsDefender += wavePoints_Defender;
					pointsAttacker += wavePoints_Attacker;
					System.out.print("Points collected this wave: "+ wavePoints_Collector+"\n");
					System.out.print("Points healer this wave: "+ wavePoints_Healer+"\n");
					System.out.print("Points defender this wave: "+ wavePoints_Defender+"\n");
					System.out.print("Points attacker this wave: "+ wavePoints_Attacker+"\n");
					System.out.print("Healer identified as: "+ totalEggsCollected+"\n");
					System.out.print("Defender identified as: "+ totalEggsCollected+"\n");
					System.out.print("Attacker identified as: "+ totalEggsCollected+"\n");
					System.out.print("Collecter identified as: "+ totalEggsCollected+"\n");
					//TODO HERE
				}

				break;

			}

		}
	}
	@Subscribe
	public void onGameTick(GameTick event)
	{
		if(scanning) {
			final String player;
			player = client.getLocalPlayer().getName();
			leader = client.getWidget(BaRoleWidget, leaderID);
			leaderIcon = client.getWidget(BaRoleWidget, leadericonID);
			player1 = client.getWidget(BaRoleWidget, player1ID);
			player1Icon = client.getWidget(BaRoleWidget, player1iconID);
			player2 = client.getWidget(BaRoleWidget, player2ID);
			player2Icon = client.getWidget(BaRoleWidget, player2iconID);
			player3 = client.getWidget(BaRoleWidget, player3ID);
			player3Icon = client.getWidget(BaRoleWidget, player3iconID);
			player4 = client.getWidget(BaRoleWidget, player4ID);
			player4Icon = client.getWidget(BaRoleWidget, player4iconID);
			log.debug("Scanning Team");

			if ((player4Icon.getModelId() != leaderIcon.getModelId()) &&  (player4Icon.getModelId() != 65535) && (leaderIcon.getModelId() != 65535)){//this number is the blank icon
				log.debug("Scanning Complete");
				log.debug("Leader is {}", leader.getText());
				log.debug("Player1 is {}", player1.getText());
				log.debug("Player2 is {}", player2.getText());
				log.debug("Player3 is {}", player3.getText());
				log.debug("Player4 is {}", player4.getText());
				scanning = false;


                    for (int i = 8; i < 13; i++)
				{
					String player_in_list = (client.getWidget(BaRoleWidget, i).getText());
					String playerRole = IDfinder(client.getWidget(BaRoleWidget, (i + 10)).getModelId());
					if (player.compareTo(player_in_list) == 0)//future developers it grabs the name from the string used in this comparison
					{
						//this checks which location the name is in the scroll
						round_roleID = client.getWidget(BaRoleWidget, (i + 10)).getModelId();
						round_role = IDfinder(round_roleID);
						log.debug("Your role has been identified as {}", round_role);
					}
				}
				if ((leaderIcon.getModelId() == attackerIcon) && (player1Icon.getModelId() == collectorIcon) && (player2Icon.getModelId() == healerIcon) && (player4Icon.getModelId() == defenderIcon))
				{

					log.debug("Leeches {} role identified as {}", player3.getText(), IDfinder(player3Icon.getModelId()));
					round_role = "Leech " + round_role;
					log.debug("This has been identified as a leech run as {}",round_role);
					leech = true;
				}


			}
		}
	}



	private String IDfinder(int roleID){
		if (roleID == attackerIcon) return "Attacker";
		if (roleID == defenderIcon) return "Defender";
		if (roleID == collectorIcon) return "Collector";
		if (roleID == healerIcon) return "Healer";
		return "";
	}


}










/*Widget rewardWidget = client.getWidget(WidgetInfo.BA_REWARD_TEXT);

				if (rewardWidget != null && rewardWidget.getText().contains(ENDGAME_REWARD_NEEDLE_TEXT) && gameTime != null && leech && isRank())
				{

					if(queue.doesCustExist(player3.getText()))
					{
						this.queue.sendRoundMsd(leader.getText(), player1.getText(), player2.getText(), player3.getText(), player4.getText(), gameTime.getPBTime(), queue.getCustomer(player3.getText()).getPriority(), queue.getCustomer(player3.getText()).getItem());

					}
					else
					{
						this.queue.sendRoundMsd(leader.getText(), player1.getText(), player2.getText(), player3.getText(), player4.getText(), gameTime.getPBTime(), "Unknown", "Unknown");

					}
					gameTime = null;
					leech = false;
				}

				break;*/