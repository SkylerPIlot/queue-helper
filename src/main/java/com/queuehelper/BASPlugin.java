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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import static net.runelite.api.widgets.WidgetInfo.TO_CHILD;
import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;
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
public class BASPlugin extends Plugin implements ActionListener
{
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

	public BASPlugin() throws IOException
	{
	}

	@Provides
    BASConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BASConfig.class);
    }

	private Queue queue;

    protected void startUp() throws Exception
    {
    	if(!isConfigApiEmpty())
		{
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

    protected void shutDown() throws Exception
    {
		clientToolbar.removeNavigation(navButton);
		this.queue = null;
		httpclient = null;

    }


	//checks on startup of plugin and throws an error
    private boolean isConfigApiEmpty(){

        if(config.apikey().equals("Paste your key here") || config.apikey().equals("")){

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
    public void onConfigChanged(ConfigChanged event) throws IOException
	{
		this.fontSize = config.fontSize();
    }

	@Subscribe
	public void onFriendsChatMemberJoined(FriendsChatMemberJoined event) throws IOException
	{
		this.queue.ShouldUpdate(true);
	}


    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event) throws IOException
	{
		this.queue.ShouldUpdate(true);
    }

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (event.getMenuEntries().length < 2)
		{
			return;
		}

		final MenuEntry entry = event.getMenuEntries()[event.getMenuEntries().length - 2];

		if (entry.getType() != MenuAction.CC_OP_LOW_PRIORITY && entry.getType() != MenuAction.RUNELITE)
		{
			return;
		}

		final int groupId = TO_GROUP(entry.getParam1());
		final int childId = TO_CHILD(entry.getParam1());

		if (groupId != WidgetInfo.CHATBOX.getGroupId())
		{
			return;
		}

		final Widget widget = client.getWidget(groupId, childId);
		final Widget parent = widget.getParent();

		if (WidgetInfo.CHATBOX_MESSAGE_LINES.getId() != parent.getId())
		{
			return;
		}

		final int first = WidgetInfo.CHATBOX_FIRST_MESSAGE.getChildId();

		final int dynamicChildId = (childId - first) * 4;

		final Widget messageContents = parent.getChild(dynamicChildId);
		if (messageContents == null)
		{
			return;
		}

		String playerName = messageContents.getText();
		String player = playerName.replaceAll("\\[.*\\]", "").trim().replace(":", "");

		client.createMenuEntry(1)
			.setOption("Add to Queue Sidepanel")
			.setTarget(entry.getTarget())
			.setType(MenuAction.RUNELITE)
			.onClick(e ->
			{
				this.basQueuePanel.changeCustomerText(Text.removeTags(Text.toJagexName(player)));
			});
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final int componentId = event.getActionParam1();
		int groupId = WidgetInfo.TO_GROUP(componentId);
		String option = event.getOption();

		if (groupId == WidgetInfo.FRIENDS_LIST.getGroupId() || groupId == WidgetInfo.FRIENDS_CHAT.getGroupId()
			|| componentId == WidgetInfo.CLAN_MEMBER_LIST.getId() || componentId == WidgetInfo.CLAN_GUEST_MEMBER_LIST.getId())
		{
			boolean after;

			if (AFTER_OPTIONS.contains(option))
			{
				after = true;
			}
			else if (BEFORE_OPTIONS.contains(option))
			{
				after = false;
			}
			else
			{
				return;
			}

			client.createMenuEntry(after ? -2 : -1)
				.setOption("Add to Queue Sidepanel")
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					this.basQueuePanel.changeCustomerText(Text.removeTags(Text.toJagexName(event.getTarget())).replaceAll("\\[.*\\]", "").trim().replace(":", ""));
				});
		}
	}


	//used in sending discord webhook messages
    private boolean isRank()
    {
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        return this.client.getLocalPlayer().getName() != null && clanMemberManager != null && clanMemberManager.getCount() >= 1 && clanMemberManager.getOwner().equals(ccName);
    }

	//builds a stringbuilder that is then passed to the Implementation of BASHTTPClient to call the backend
    public void updateQueue() throws IOException
	{
		if(!isRank()){
			return;
		}
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        if (!this.config.autoUpdateQueue() || clanMemberManager == null)
            return;
        StringBuilder csv = new StringBuilder();
        for (FriendsChatMember member : (FriendsChatMember[]) clanMemberManager.getMembers())
        {
            String memberName = member.getName();
            if (csv.toString().equals(""))
            {
                csv = new StringBuilder(memberName + "#" + member.getRank().getValue());
            } else
            {
                csv.append(",").append(memberName).append("#").append(member.getRank().getValue());
            }
        }
        if (csv.toString().equals(""))
            return;
        if (isConfigApiEmpty()){
            return;
        }
        String name = config.queueName();
        if(client.getLocalPlayer().getName() != null){
			name = Text.sanitize(client.getLocalPlayer().getName());
		}

        queue.updateQueuebackend(csv, name);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
		if(!isRank() || chatMessage.getType() != ChatMessageType.FRIENDSCHAT)
		{
			return;
		}

		FriendsChatRank rank = getRank(chatMessage.getName());
		if (isConfigApiEmpty()){
			return;
		}//TODO fix hanging

		try{
			int numMsg = (int) chatMessage.getMessage().charAt(0);
			if((48 <= numMsg && numMsg <= 53) && (chatMessage.getMessage().contains("out") || chatMessage.getMessage().contains("f") || chatMessage.getMessage().contains("a") || chatMessage.getMessage().contains("*") || chatMessage.getMessage().contains("c") || chatMessage.getMessage().contains("d") || chatMessage.getMessage().contains("h") || (chatMessage.getMessage().contains("r") && !chatMessage.getMessage().contains("reg"))))
			{
				if(48 <= ((int) chatMessage.getMessage().charAt(1)) && ((int) chatMessage.getMessage().charAt(1)) <= 57){
					msgIn = false;
				}
				else{
					msgIn = true;
				}
			}
		}
		catch (NumberFormatException ex){
			log.debug("Normal behavior");
		}


		try{
			int numMsg = Integer.parseInt(chatMessage.getMessage());
			if(0 <= numMsg  && numMsg <= 5)
			{
				msgIn = true;
			}
		}
		catch (NumberFormatException ex){
			log.debug("Normal behavior");
		}

		if (chatMessage.getMessage().toLowerCase().contains("t+") || chatMessage.getMessage().toLowerCase().contains("-=-=") || chatMessage.getMessage().toLowerCase().contains("---") || chatMessage.getMessage().toLowerCase().contains("===") || chatMessage.getMessage().toLowerCase().equals("jf") || chatMessage.getMessage().toLowerCase().equals("out")){
			msgIn = true;
		}




		if (((chatMessage.getMessage().contains("+") && chatMessage.getMessage().charAt(0) == '+') || msgIn) && !chatMessage.getMessage().toLowerCase().contains("@"))
		{
			msgIn = false;
			queue.sendChatMsgDiscord(chatMessage);



		}
	//TODO implement webhook + blairm messages + retrieving and using reuls from the AWS server
    }

	private FriendsChatRank getRank(String playerName)
	{
		FriendsChatManager friendsChatManager = this.client.getFriendsChatManager();
		if (friendsChatManager == null)
			return FriendsChatRank.UNRANKED;
		FriendsChatMember friendsChatMember = (FriendsChatMember) friendsChatManager.findByName(playerName);
		return (friendsChatMember != null) ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		//required, didn't feel like using instead used specific functions
	}

	//used in BasQueueRow to run the "Next" button
	public void getNext(){
    	Customer next = queue.getNext();
    	if (next == null) {
    		sendChat("queue empty");
    		return;
		}
		sendChat("Next customer in line: Priority " + next.getPriority() + " " + next.getName() + " " + next.getItem() + " " + next.getNotes());
	}

	public void sendChat(String msg){
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append(msg).build();
    	BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	//Adds a customer to the queue and is called by the addcustomer button in the basQueuePanel
	public void addToQueue(String name, String item, String priority){
    	if(name.equals("Customer")){
    		sendChat("Please enter a name");
    		return;
		}
    	if(queue.addToQueue(item, priority, name, config.queueName())){
    		sendChat("Added: " + name + " for " + priority + " " + item);

		}
    	else{
			sendChat("Failed to add: " + name + " for " + priority + " " + item);
		}
		refreshQueue();

	}
	//used in BasQueueRow to run the "Refresh" button
	public void refreshQueue()
	{
		//creates a list of online nonranks to update a autocomplete function
		if(client.getGameState() == GameState.LOGGED_IN)
		{
			FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
			FriendsChatMember[] memberlist = clanMemberManager.getMembers();
			ArrayList<String> keywords = new ArrayList<>();
			for (FriendsChatMember member : memberlist)
			{
				if (member.getRank() == FriendsChatRank.UNRANKED && !queue.doesCustExist(member.getName()))
				{
					keywords.add(member.getName());
				}
			}
			basQueuePanel.setAutoCompleteKeyWords(keywords);
		}
		try
		{
			queue.refresh();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}
	//used in BasQueueRow to run the right click options
	public void markCustomer(int option, Customer cust)
	{
		int UNSUPORRTED = 0;
		if(option != UNSUPORRTED)
		{
			try
			{
				queue.mark(option, cust);
			}
			catch (IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
		this.refreshQueue();
		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
	}
}
