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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.ui.NavigationButton;
import okhttp3.OkHttpClient;

public class Queue
{
	private static Queue queue;
	private boolean shouldUpdateQueue;
	private BasQueuePanel basPanel;

	private OkHttpClient RLhttpclient;

	Customer tempNext;
	boolean first = true;

	private LinkedHashMap<String, Customer> CurrentQueue;//Queue saved via insertion order

	private List<String[]> OldQueue; //Old csv data used, used in comparison in our implemented HTTPclient interface

	private QueueHelperHTTPClient httpClient;

	Timer timer;

	TimerTask updateQueue;

	BASPlugin plugin;

	private Queue(String apikey, BasQueuePanel BasPanel, BASPlugin Plugin, OkHttpClient rlhttp) throws IOException
	{
		this.plugin = Plugin;
		this.RLhttpclient = rlhttp;
		this.basPanel = BasPanel;
		this.timer = new Timer();
		this.shouldUpdateQueue = false;
		this.CurrentQueue = new LinkedHashMap<String, Customer>();
		this.httpClient = BASHTTPClient.getInstance(apikey, RLhttpclient);
		this.OldQueue = new ArrayList<>();
		createQueue();
		this.updateQueue = new TimerTask()
		{
			@Override
			public void run()
			{
				updateQueueTask(ShouldUpdate());
			}
		};
		timer.scheduleAtFixedRate(this.updateQueue,new Date(),12000);//Schedules a task every 2minutes to both refresh queue object + upload the cc data -> backend
	}

	public static Queue getInstance(String apikey, BasQueuePanel BasPanel, BASPlugin basPlugin,OkHttpClient rlhttp) throws IOException //Singleton queue creation should only need one queue per plugin
	{
		if (Queue.queue == null)
		{
			Queue.queue = new Queue(apikey, BasPanel, basPlugin,rlhttp);
		}
		else
		{
			Queue.queue.setAPikey(apikey);
		}
		return Queue.queue;
	}

	public boolean ShouldUpdate(){
		return this.shouldUpdateQueue;
	}

	public void ShouldUpdate(boolean ShouldUpdateQueue){
		this.shouldUpdateQueue = ShouldUpdateQueue;
	}

	private void setAPikey(String apikey) throws IOException
	{
		this.httpClient = BASHTTPClient.getInstance(apikey,this.RLhttpclient);
	}

	public boolean doesCustExist(String name){
		return CurrentQueue.containsKey(name);
	}

	private void createQueue() throws IOException
	{
		this.OldQueue = httpClient.readCSV(this.OldQueue);
		if(this.OldQueue == null){
			return;
		}
		for (String[] CSVLine : this.OldQueue)
		{
			try {
				this.CurrentQueue.put(CSVLine[1], new Customer(CSVLine[1], CSVLine[3], CSVLine[0], CSVLine[2], CSVLine[5], CSVLine[4]));
			} catch (Exception e) {
				/*for(String line:CSVLine){
					System.out.print(line+",");
				}
				System.out.print("\n");*/
				if(CSVLine[0].equals("P")||CSVLine[0].equals("R")) {
					this.CurrentQueue.put(CSVLine[1], new Customer(CSVLine[1], CSVLine[3], CSVLine[0], CSVLine[2], " ", CSVLine[4]));
				}
				//this catches some suboptimal creation actuall yhanldes when ppl use , in the notes column
            }

        }
	}

	public NavigationButton getNav()
	{
		return httpClient.getNavButton(); //return the navigation button from the httpclient implementation allows for alternative nav buttons
	}

	public LinkedHashMap<String, Customer> getQueue()
	{
		return this.CurrentQueue;
	}

	public Customer getNext()
	{

		for (Customer cust : this.CurrentQueue.values())
		{
			if (cust.getStatus().equals("Online") && cust.getPriority().equals("P") && !cust.getNotes().toLowerCase().contains("cooldown"))
			{
				return cust;
			}
				if (cust.getStatus().equals("Online") && cust.getPriority().equals("R") && !cust.getNotes().toLowerCase().contains("cooldown") && first)
			{
				first = false;
				tempNext = cust;//sets the first regular customer to the returned name if no premiums exist
			}


		}
		first = true;
		return tempNext;

	}

	public void refresh() throws IOException
	{
		this.plugin.updateQueue();
		this.CurrentQueue.clear();
		this.createQueue();
	}

	public void mark(int option, Customer cust,String rankname) throws IOException
	{
		this.httpClient.markCustomer(option, cust.getName(), rankname);
	}
	//scheduled task every2minutes Both redraws the panel after retrieving uptodate queue + sends data to the queue given someone has left/joined the cc
	private void updateQueueTask(boolean shouldUpdateQueue)
	{
		if(shouldUpdateQueue){
			try
			{
				this.plugin.updateQueue();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.shouldUpdateQueue = false;
		}
		try
		{
			createQueue();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> this.basPanel.populate(queue.getQueue()));
	}

	public boolean addToQueue(String itemName, String priority, String custName, String addedBy){
		try
		{
			return httpClient.addCustomer(itemName, priority, custName, addedBy);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void updateQueuebackend(StringBuilder urlList, String name) throws IOException
	{
		httpClient.updateQueuebackend(urlList,name);
	}

	public void sendChatMsgDiscord(ChatMessage chatMessage){
		try
		{
			httpClient.sendChatMsgDiscord(chatMessage);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void exportCSV() throws IOException {
		StringBuilder csvBuilder = new StringBuilder();
		// Optional header row
		csvBuilder.append("Priority,Customer,Status,ID,Item,Notes\n");

		// Assuming CurrentQueue is declared as, for example:
		// private LinkedHashMap<String, Customer> CurrentQueue;
		for (Customer cust : this.CurrentQueue.values()) {
			csvBuilder.append(cust.getPriority()).append(",")
					.append(cust.getName()).append(",")
					.append(cust.getStatus()).append(",")
					.append(cust.getID()).append(",")
					.append(cust.getItem()).append(",")
					.append(cust.getNotes()).append("\n");
		}

		// Upload the new CSV to S3 using your existing method.
		// For example:
		httpClient.updateQueuebackend(csvBuilder, "queue.csv");
	}
}
