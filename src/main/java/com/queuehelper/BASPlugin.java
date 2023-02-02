package com.queuehelper;

import com.google.common.collect.ImmutableList;

import com.google.inject.Provides;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
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
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.Text;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(name = "BAS Queue Helper", description = "BAS Customer CC Info", tags = {"minigame"})
public class BASPlugin extends Plugin implements ActionListener, KeyListener
{
    private static final Logger log = LoggerFactory.getLogger(BASPlugin.class);

    private static final String ccName = "Ba Services";

    private static final String errorMsg = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("BAS QH: ").append(ChatColorType.HIGHLIGHT).append("Please Paste the API key in the plugin settings and restart the plugin").build();

	private BasQueuePanel basQueuePanel;
	private NavigationButton navButton;


	private BASHTTPClient httpclient;

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
			this.queue = Queue.getInstance(config.apikey(), basQueuePanel, this);
			//resourcePacksHubPanel = injector.getInstance(ResourcePacksHubPanel.class);
			navButton = queue.getNav();
			navButton.setPanel(basQueuePanel);
			clientToolbar.addNavigation(navButton);
			SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));

		}
    }

    protected void shutDown() throws Exception
    {
		clientToolbar.removeNavigation(navButton);
		this.queue = null;
		httpclient = null;
        this.keyManager.unregisterKeyListener(this);



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


    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined event) throws IOException
	{
		this.queue.ShouldUpdate(true);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {

		SwingUtilities.invokeLater(() -> basQueuePanel.populate(queue.getQueue()));
    }

    @Subscribe
    public void onFriendsChatMemberLeft(FriendsChatMemberLeft event) throws IOException
	{
		this.queue.ShouldUpdate(true);
    }



    private boolean isRank()
    {
        FriendsChatManager clanMemberManager = this.client.getFriendsChatManager();
        return this.client.getLocalPlayer().getName() != null && clanMemberManager != null && clanMemberManager.getCount() >= 1 && clanMemberManager.getOwner().equals(ccName);
    }


    private void addCustomerToQueue(final String name, final String item)
    {
        if (isConfigApiEmpty()){
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

        //final HttpUrl httpUrl = (new HttpUrl.Builder()).scheme("https").host("HOST_PATH").addPathSegment("Bas_Queuehelper").addPathSegment("bas").addPathSegment('updateFile').addQueryParameter('UPDATE_OPTION_ATQ', "1").addQueryParameter('UPDATE_OPTION_PRI', priority).addQueryParameter(UPDATE_OPTION_NAM, name.replace(' ', ' ')).addQueryParameter(UPDATE_OPTION_FORMI, formItem).addQueryParameter(UPDATE_OPTION_QN, queueName).build();
        Request request = (new Request.Builder()).header("User-Agent", "RuneLite").header("x-api-key", config.apikey()).url("httpUrl").build();
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
                String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Sent a request to add " + name + " for " + item + ".").build();
                BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
                //BASPlugin.this.getCustomerID(name);
            }
        });
    }


    public void updateQueue() throws IOException
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
        String name = config.queueName();
        if(client.getLocalPlayer().getName() != null){
			name = Text.sanitize(client.getLocalPlayer().getName());
		}

        queue.updateQueuebackend(csv, name);
    }




    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {

    }

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
    }

    public void keyReleased(KeyEvent e)
    {
    }

	@Override
	public void actionPerformed(ActionEvent e)
	{
		//required, didn't feel like using instead used specific functions
	}


	public void getNext(){
    	Customer next = queue.getNext();
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append("Next customer in line: ").append(ChatColorType.HIGHLIGHT).append(next.getName() + " " + next.getItem() + " " + next.getNotes()).build();
		BASPlugin.this.chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	public void refreshQueue()
	{
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

	public void markCustomer(int option, Customer cust)
	{
		if(option != 0)
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
