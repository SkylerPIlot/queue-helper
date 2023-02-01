package com.queuehelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.runelite.client.ui.NavigationButton;

public class Queue
{
	private static Queue queue;

	private LinkedHashMap<String, Customer> CurrentQueue;//Queue saved via insertion order

	private List<String[]> OldQueue; //Old csv data used, used in comparison in our implemented HTTPclient interface

	private QueueHelperHTTPClient httpClient; //TODO change this to an interface and create the queue object via a specific interface

	private Queue(String apikey) throws IOException
	{
		this.CurrentQueue = new LinkedHashMap<String, Customer>();
		this.httpClient = BASHTTPClient.getInstance(apikey);
		this.OldQueue = new ArrayList<>();
		createQueue();
	}

	public static Queue getInstance(String apikey) throws IOException //Singleton queue creation should only need one queue per plugin
	{
		if(Queue.queue == null){
			Queue.queue = new Queue(apikey);
		}
		else{
			Queue.queue.setAPikey(apikey);
		}
		return Queue.queue;
	}

	private void setAPikey(String apikey) throws IOException
	{
		this.httpClient = BASHTTPClient.getInstance(apikey);
	}

	private void createQueue() throws IOException
	{
		//TODO remove the need for readCSV to have a List<String[]> passed into it
		this.OldQueue = httpClient.readCSV(this.OldQueue);
		for(String[] CSVLine : this.OldQueue){
			this.CurrentQueue.put(CSVLine[1], new Customer(CSVLine[1], CSVLine[3], CSVLine[0], CSVLine[2], CSVLine[5],CSVLine[4]));
		}
	}

	public String getIDfromName(String name){
		return this.CurrentQueue.get(name).getID();
	}

	public String getPriorityfromName(String name){
		return this.CurrentQueue.get(name).getPriority();
	}

	public boolean doesCustomerExist(String name){
		return this.CurrentQueue.containsKey(name);
	}

	public NavigationButton getNav(){
		return httpClient.getNavButton();
	}

	public LinkedHashMap<String, Customer> getQueue(){
		return this.CurrentQueue;
	}


}
