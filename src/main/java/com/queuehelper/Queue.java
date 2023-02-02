package com.queuehelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import lombok.SneakyThrows;
import net.runelite.client.ui.NavigationButton;

public class Queue
{
	private static Queue queue;
	private boolean shouldUpdateQueue;
	private BasQueuePanel basPanel;

	Customer tempNext;
	boolean first = true;

	private LinkedHashMap<String, Customer> CurrentQueue;//Queue saved via insertion order

	private List<String[]> OldQueue; //Old csv data used, used in comparison in our implemented HTTPclient interface

	private QueueHelperHTTPClient httpClient; //TODO change this to an interface and create the queue object via a specific interface

	Timer timer;

	TimerTask updateQueue;

	BASPlugin plugin;

	private Queue(String apikey, BasQueuePanel BasPanel, BASPlugin Plugin) throws IOException
	{
		this.plugin = Plugin;
		this.basPanel = BasPanel;
		this.timer = new Timer();
		this.shouldUpdateQueue = false;
		this.CurrentQueue = new LinkedHashMap<String, Customer>();
		this.httpClient = BASHTTPClient.getInstance(apikey);
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
		timer.scheduleAtFixedRate(this.updateQueue,new Date(),120000);
	}

	public static Queue getInstance(String apikey, BasQueuePanel BasPanel, BASPlugin basPlugin) throws IOException //Singleton queue creation should only need one queue per plugin
	{
		if (Queue.queue == null)
		{
			Queue.queue = new Queue(apikey, BasPanel, basPlugin);
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
		this.httpClient = BASHTTPClient.getInstance(apikey);
	}

	private void createQueue() throws IOException
	{
		//TODO remove the need for readCSV to have a List<String[]> passed into it
		this.OldQueue = httpClient.readCSV(this.OldQueue);
		for (String[] CSVLine : this.OldQueue)
		{
			this.CurrentQueue.put(CSVLine[1], new Customer(CSVLine[1], CSVLine[3], CSVLine[0], CSVLine[2], CSVLine[5], CSVLine[4]));
		}
	}

	public String getIDfromName(String name)
	{
		return this.CurrentQueue.get(name).getID();
	}

	public String getPriorityfromName(String name)
	{
		return this.CurrentQueue.get(name).getPriority();
	}

	public boolean doesCustomerExist(String name)
	{
		return this.CurrentQueue.containsKey(name);
	}

	public NavigationButton getNav()
	{
		return httpClient.getNavButton();
	}

	public LinkedHashMap<String, Customer> getQueue()
	{
		return this.CurrentQueue;
	}

	public Customer getNext()
	{

		for (Customer cust : this.CurrentQueue.values())
		{
			if (cust.getStatus().equals("Online") && cust.getPriority().equals("P"))
			{
				return cust;
			}
			if (cust.getStatus().equals("Online") && cust.getPriority().equals("R") && first)
			{
				first = false;
				tempNext = cust;
			}


		}
		first = true;
		return tempNext;

	}

	public void refresh() throws IOException
	{
		this.CurrentQueue.clear();
		this.createQueue();
	}

	public void mark(int option, Customer cust) throws IOException
	{
		this.httpClient.markCustomer(option, cust.getName());
	}
	//scheduled task every2minutes
	private void updateQueueTask(boolean shouldUpdateQueue)
	{
		if(shouldUpdateQueue){
			try
			{
				createQueue();
				this.plugin.updateQueue();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			SwingUtilities.invokeLater(() -> this.basPanel.populate(queue.getQueue()));
			this.shouldUpdateQueue = false;
		}
	}
	public void updateQueuebackend(StringBuilder urlList, String name) throws IOException
	{
		httpClient.updateQueuebackend(urlList,name);
	}
}
