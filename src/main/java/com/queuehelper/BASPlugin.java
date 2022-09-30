package com.queuehelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import okhttp3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(name = "BAS Queue Helper", description = "BAS Customer CC Info", tags = {"minigame"})
public class BASPlugin extends Plugin implements KeyListener
{
    private static final Logger log = LoggerFactory.getLogger(BASPlugin.class);

	private String HOST_PATH = "bas-qh.herokuapp.com";

    private static final String ccName = "Ba Services";

	private String UPDATE_OPTION_A = "";

	private String UPDATE_OPTION_QSPR = "";

	private String UPDATE_OPTION_GNC = "";

	private String UPDATE_OPTION_ATQ = "";

	private String UPDATE_OPTION_PRI = "";

	private String UPDATE_OPTION_NAM = "";

	private String UPDATE_OPTION_FORMI = "";

	private String UPDATE_OPTION_QN = "";

	private String UPDATE_OPTION_D = "";

	private String UPDATE_OPTION_QHN = "";

	private String UPDATE_OPTION_C = "";

	private String UPDATE_OPTION_M = "";

	private String UPDATE_OPTION_R = "";

	private String UPDATE_OPTION_O = "";

	private String UPDATE_OPTION_CN = "";

	private String updateFile = "";

    private static final int clanSetupWidgetID = 20;

    private static final ImmutableList<String> BAS_OPTIONS = ImmutableList.of("Mark Done", "In-Progress", "Mark Online", "Start Cooldown", "Get Customer ID");

    private static final ImmutableList<String> BAS_BUY_OPTIONS = ImmutableList.of("Prem 1R Points", "Reg 1R Points", "Prem Hat", "Reg Hat", "Prem Queen Kill", "Reg Queen Kill", "Prem Lvl 5s", "Reg Lvl 5s", "Prem Torso", "Reg Torso");

    private final List<String[]> csvContent = new ArrayList<>();

    private final List<String> ccMembersList = new ArrayList<>();

    private static final String errorMsg = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("BAS QH: ").append(ChatColorType.HIGHLIGHT).append("Please Paste the API key in the plugin settings and restart the plugin").build();

    private int lastCheckTick;

    private int ccCount;

    private static final ImmutableList<String> AFTER_OPTIONS = ImmutableList.of("Message", "Add ignore", "Remove friend", "Kick");

    private boolean shiftDown;

    private boolean isUpdated = true;

    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private BASConfig config;

    @Inject
    private KeyManager keyManager;

	@Inject
	private OkHttpClient BasHttpClient;

    @Provides
    BASConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BASConfig.class);
    }

    protected void startUp() throws Exception
    {
        this.keyManager.registerKeyListener(this);
        this.isUpdated = true;
		getupdateStrings();
    }

    protected void shutDown() throws Exception
    {
        this.keyManager.unregisterKeyListener(this);
        ccUpdate();
        this.csvContent.clear();
		clearupdateStrings();
    }

	private boolean getupdateStrings(){

        if (isConfigApiEmpty()){
            return false;

        }

		OkHttpClient httpClient = BasHttpClient;
		HttpUrl httpUrl = new HttpUrl.Builder()
			.scheme("http")
			.host(HOST_PATH)
			.addPathSegment("bas")
			.addPathSegment("grabfilestrings")
			.build();


		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(httpUrl)
			.header("APIKEY", config.apikey())
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Error sending http request1.", e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				BufferedReader in = new BufferedReader(new StringReader(response.body().string()));
				String[] splitString = in.readLine().split(",");
				UPDATE_OPTION_QSPR = splitString[0];

				UPDATE_OPTION_GNC = splitString[1];

				UPDATE_OPTION_ATQ = splitString[2];

				UPDATE_OPTION_PRI = splitString[3];

				UPDATE_OPTION_NAM = splitString[4];

				UPDATE_OPTION_FORMI = splitString[5];

				UPDATE_OPTION_QN = splitString[6];

				UPDATE_OPTION_D = splitString[7];

				UPDATE_OPTION_QHN = splitString[8];

				UPDATE_OPTION_C = splitString[9];

				UPDATE_OPTION_M = splitString[10];

				UPDATE_OPTION_R = splitString[11];

				UPDATE_OPTION_O = splitString[12];

				UPDATE_OPTION_CN = splitString[13];

				updateFile = splitString[14];

				UPDATE_OPTION_A = splitString[15];

				response.close();
			}
		});

		if(UPDATE_OPTION_GNC.equals("")){
		    return false;
        }// this lets us know if this update was succesfull or not
		return true;
	}

	private boolean checkSuccesfulConnection(){
        if (UPDATE_OPTION_GNC.equals("")){
            if (getupdateStrings()){
                return true;//was succesfull in updating after being in a failure/starting state
            }
            return false;
        }
        return true; // was already succesfully verified
    }

    private boolean isConfigApiEmpty(){

        if(config.apikey().equals("Paste your key here") || config.apikey().equals("")){

            BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                    .type(ChatMessageType.CONSOLE)
                    .runeLiteFormattedMessage(errorMsg)
                    .build());
            return true;
        }
        return false;

    }

	private void clearupdateStrings(){
    	UPDATE_OPTION_QSPR = "";

    	UPDATE_OPTION_GNC = "";

    	UPDATE_OPTION_ATQ = "";

    	UPDATE_OPTION_PRI = "";

    	UPDATE_OPTION_NAM = "";

    	UPDATE_OPTION_FORMI = "";

    	UPDATE_OPTION_QN = "";

    	UPDATE_OPTION_D = "";

	    UPDATE_OPTION_QHN = "";

    	UPDATE_OPTION_C = "";

	    UPDATE_OPTION_M = "";

	    UPDATE_OPTION_R = "";

	    UPDATE_OPTION_O = "";

    	UPDATE_OPTION_CN = "";

    	updateFile = "";
}

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!this.isUpdated || !isRank())
            return;
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        updateCCPanel();
        if (clanMemberManager != null && clanMemberManager.getCount() > 0 && this.ccCount != clanMemberManager.getCount())
        {
            ccUpdate();
            this.ccCount = clanMemberManager.getCount();
        }
        if (this.config.getNextCustomer())
        {
            Widget clanSetupWidget = this.client.getWidget(7, clanSetupWidgetID);
            if (clanSetupWidget != null)
            {
                clanSetupWidget.setText("Next Customer");
                clanSetupWidget.setHasListener(false);
            }
        }
    }

    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
    {
        ccUpdate();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
       checkSuccesfulConnection();
    }

    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event)
    {
        ccUpdate();
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (!isRank() || !this.isUpdated)
            return;
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        int groupId = WidgetInfo.TO_GROUP(event.getActionParam1());
        String option = event.getOption();
        if (groupId == WidgetInfo.FRIENDS_CHAT.getGroupId() || (groupId == WidgetInfo.CHATBOX
                .getGroupId() && !"Kick".equals(option)))
        {
            if (this.config.getNextCustomer() && groupId == WidgetInfo.FRIENDS_CHAT.getGroupId() && WidgetInfo.TO_CHILD(event.getActionParam1()) == clanSetupWidgetID && clanMemberManager != null && clanMemberManager.getOwner().equals(ccName))
            {
                client.createMenuEntry(0)
                        .setOption("Next customer")
                        .setTarget(event.getTarget());

            }
            if (!AFTER_OPTIONS.contains(option))
                return;
            if (!this.shiftDown && this.config.markCustomerOptions() && this.ccMembersList.contains(Text.removeTags(Text.sanitize(event.getTarget()))))
            {
                for (String basOption : BAS_OPTIONS)
                {
                    client.createMenuEntry(-2)
                            .setOption(basOption)
                            .setTarget(event.getTarget())
                            .setType(MenuAction.RUNELITE)
                            .setIdentifier(event.getIdentifier());
                }
            } else if (this.config.addToQueue() && !this.ccMembersList.contains(Text.removeTags(Text.sanitize(event.getTarget()))) && this.shiftDown)
            {
                for (String basOption : BAS_BUY_OPTIONS)
                {
                    if (((basOption
                            .equals("Reg Torso") || basOption.equals("Prem Torso")) && this.config.torsoOptions()) || ((basOption
                            .equals("Reg Hat") || basOption.equals("Prem Hat")) && this.config.hatOptions()) || ((basOption
                            .equals("Reg Queen Kill") || basOption.equals("Prem Queen Kill")) && this.config.qkOptions()) || ((basOption
                            .equals("Reg 1R Points") || basOption.equals("Prem 1R Points")) && this.config.OneROptions()) || ((basOption
                            .equals("Reg Lvl 5s") || basOption.equals("Prem Lvl 5s")) && this.config.Lvl5Options()))
                    {
                        client.createMenuEntry(-2)
                                .setOption(basOption)
                                .setTarget(event.getTarget())
                                .setType(MenuAction.RUNELITE)
                                .setIdentifier(event.getIdentifier());
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        String targetSanitized = Text.removeTags(Text.sanitize(event.getMenuTarget()));
        if (event.getMenuOption().equals("Next customer"))
            getNextCustomer();
        if (BAS_BUY_OPTIONS.contains(event.getMenuOption()))
            addCustomerToQueue(targetSanitized, event.getMenuOption());
        if (!BAS_OPTIONS.contains(event.getMenuOption()))
            return;
        if (event.getMenuOption().equals("Get Customer ID"))
        {
            getCustomerID(targetSanitized);
            return;
        }
        String appendMessage = "";
        switch (event.getMenuOption())
        {
            case "In-Progress":
                appendMessage = "in progress.";
                markCustomer(1, targetSanitized);
                break;
            case "Mark Done":
                appendMessage = "done.";
                markCustomer(2, targetSanitized);
                break;
            case "Mark Online":
                appendMessage = "online.";
                markCustomer(3, targetSanitized);
                break;
            case "Start Cooldown":
                appendMessage = "start cooldown.";
                markCustomer(4, targetSanitized);
                break;
        }
        String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Marked " + targetSanitized + " as ").append(ChatColorType.HIGHLIGHT).append(appendMessage).build();
        this.chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(chatMessage)
                .build());
    }

    private boolean isRank()
    {
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        return this.client.getLocalPlayer().getName() != null && clanMemberManager != null && clanMemberManager.getCount() >= 1 && clanMemberManager.getOwner().equals(ccName) && this.isUpdated;
    }

    private void getNextCustomer()
    {
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_GNC, "1").build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                BufferedReader in = new BufferedReader(new StringReader(response.body().string()));
                String CustId = "";
                String s;
                while ((s = in.readLine()) != null)
                    CustId = s;
                String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Next customer in line: ").append(ChatColorType.HIGHLIGHT).append(CustId).build();
                BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                response.close();
            }
        });
    }

    private void addCustomerToQueue(final String name, final String item)
    {
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        String queueName = this.config.queueName().equals("") ? this.client.getLocalPlayer().getName() : this.config.queueName();
        String formItem = "";
        String priority = "Regular";
        switch (item)
        {
            case "Reg Hat":
                formItem = "Hat";
                break;
            case "Reg Lvl 5s":
                formItem = "Level 5 Roles";
                break;
            case "Reg Queen Kill":
                formItem = "Queen Kill - Diary";
                break;
            case "Reg Torso":
                formItem = "Torso";
                break;
            case "Reg 1R Points":
                formItem = "One Round - Points";
                break;
            case "Prem Hat":
                priority = "Premium";
                formItem = "Hat";
                break;
            case "Prem Lvl 5s":
                priority = "Premium";
                formItem = "Level 5 Roles";
                break;
            case "Prem Queen Kill":
                priority = "Premium";
                formItem = "Queen Kill - Diary";
                break;
            case "Prem Torso":
                priority = "Premium";
                formItem = "Torso";
                break;
            case "Prem 1R Points":
                priority = "Premium";
                formItem = "One Round - Points";
                break;
        }
        final HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_ATQ, "1").addQueryParameter(UPDATE_OPTION_PRI, priority).addQueryParameter(UPDATE_OPTION_NAM, name.replace(' ', ' ')).addQueryParameter(UPDATE_OPTION_FORMI, formItem).addQueryParameter(UPDATE_OPTION_QN, queueName).build();
        Request request = (new Request.Builder()).url(httpUrl).build();
		BasHttpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                BASPlugin.log.debug("failed customer to queue");
            }

            public void onResponse(Call call, Response response)
            {
                response.close();
                BASPlugin.log.debug("added customer to queue");
                Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
                String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Sent a request to add " + name + " for " + item + ".").build();
                BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                BASPlugin.this.getCustomerID(name);
            }
        });
    }

    private void insertMenuEntry(MenuEntry newEntry, MenuEntry[] entries, boolean swap)
    {
        MenuEntry[] newMenu = (MenuEntry[]) ObjectArrays.concat((Object[]) entries, newEntry);
        int menuEntryCount = newMenu.length;
        if (swap)
            ArrayUtils.swap((Object[]) newMenu, menuEntryCount - 1, menuEntryCount - 2);
        this.client.setMenuEntries(newMenu);
    }

    private void ccUpdate()
    {
        if (this.lastCheckTick == this.client.getTickCount() || !isRank() || !this.isUpdated)
            return;
        readCSV();
        updateQueue();
        this.lastCheckTick = this.client.getTickCount();
    }

    private void updateCCPanel()
    {
        Widget clanChatWidget = this.client.getWidget(WidgetInfo.FRIENDS_CHAT);
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        if (clanChatWidget != null && !clanChatWidget.isHidden())
        {
            Widget clanChatList = this.client.getWidget(WidgetInfo.FRIENDS_CHAT_LIST);
            Widget owner = this.client.getWidget(WidgetInfo.FRIENDS_CHAT_OWNER);
            if (clanMemberManager != null && clanMemberManager.getCount() > 0)
            {
                assert owner != null;
                if (owner.getText().equals("<col=ffb83f>Ba Services</col>"))
                {
                    assert clanChatList != null;
                    Widget[] membersWidgets = clanChatList.getDynamicChildren();
                    for (Widget member : membersWidgets)
                    {
                        if (member.getTextColor() == 16777215)
                        {
                            int lineNum = 1;
                            for (String[] user : this.csvContent)
                            {
                                int spreadsheetIgnoreLines = 4;
                                if (lineNum++ >= spreadsheetIgnoreLines) {
                                    if (user[1].toLowerCase().contains(member.getText().toLowerCase())) {
                                        if (!this.ccMembersList.contains(member.getText()))
                                            this.ccMembersList.add(member.getText());
                                        switch (user[2]) {
                                            case "":
                                                member.setText(member.getText() + " (U)");
                                                break;
                                            case "Online":
                                                member.setText(member.getText() + " (O)");
                                                break;
                                            case "In Progress":
                                                member.setText(member.getText() + " (P)");
                                                break;
                                        }
                                        if (user[0].equals("P")) {
                                            //member.setTextColor(6604900);
											member.setTextColor(6579400);//sets both the same color/removes prem
                                            continue;
                                        }
                                        member.setTextColor(6579400);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void readCSV()
    {
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;//often failing method(slow server) opting to reduce requests
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_QSPR, "1").build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                BASPlugin.log.warn("Error sending http request2." + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                BufferedReader in = new BufferedReader(new StringReader(response.body().string()));
                int lineNum = 0;
                BASPlugin.this.csvContent.clear();
                String s;
                while ((s = in.readLine()) != null)
                {
                    String[] splitString = s.split(",");
                    if (splitString.length > 5)
                        BASPlugin.this.csvContent.add(new String[] {splitString[2], splitString[2].equals("R") ? splitString[4] : splitString[3], splitString[0]});
                }
                response.close();
            }
        });
    }

    private void updateQueue()
    {
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
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_D, csv.toString()).addQueryParameter(UPDATE_OPTION_QHN, Text.sanitize(this.client.getLocalPlayer().getName())).build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        log.debug("sending: " + httpUrl.toString());
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                BASPlugin.log.warn("Error sending http request3." + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                response.close();
            }
        });
    }

    private void markCustomer(int option, String name)
    {
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_O, option + "").addQueryParameter(UPDATE_OPTION_CN, name).build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        log.debug("marking: " + httpUrl.toString());
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                BASPlugin.log.warn("Error sending http request4." + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                response.close();
            }
        });
    }

    private void getCustomerID(final String name)
    {
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_A, name.replace(' ', ' ')).build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Error getting ID for " + name).build();
                BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                BufferedReader in = new BufferedReader(new StringReader(response.body().string()));
                String CustId = "";
                String s;
                while ((s = in.readLine()) != null)
                    CustId = s;
                String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("ID # for " + name + ": ").append(ChatColorType.HIGHLIGHT).append(CustId).build();
                BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                response.close();
            }
        });
    }

    private FriendsChatRank getRank(String playerName)
    {
        FriendsChatManager friendsChatManager = this.client.getFriendsChatManager();
        if (friendsChatManager == null)
            return FriendsChatRank.UNRANKED;
        FriendsChatMember friendsChatMember = (FriendsChatMember) friendsChatManager.findByName(playerName);
        return (friendsChatMember != null) ? friendsChatMember.getRank() : FriendsChatRank.UNRANKED;
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (!isRank() || !this.isUpdated || chatMessage.getType() != ChatMessageType.FRIENDSCHAT)
            return;
        FriendsChatRank rank = getRank(chatMessage.getName());
        if (isConfigApiEmpty()){
            return;
        }
        if(!checkSuccesfulConnection()){
            return;
        }
        OkHttpClient httpClient = BasHttpClient;
        HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("http").host(HOST_PATH).addPathSegment("bas").addPathSegment(updateFile).addQueryParameter(UPDATE_OPTION_C, chatMessage.getMessage()).addQueryParameter(UPDATE_OPTION_M, Text.removeTags(chatMessage.getName())).addQueryParameter(UPDATE_OPTION_R, Integer.toString(rank.getValue())).build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("APIKEY", config.apikey()).url(httpUrl).build();
        httpClient.newCall(request).enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
//                BASPlugin.log.warn("Error sending http request5." + e.getMessage());
            }

            public void onResponse(Call call, Response response) throws IOException
            {
                response.close();
            }
        });
    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == 16)
            this.shiftDown = true;
    }

    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == 16)
            this.shiftDown = false;
    }
}
