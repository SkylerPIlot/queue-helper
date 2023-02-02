package com.queuehelper;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import net.runelite.client.ui.NavigationButton;


public interface QueueHelperHTTPClient
{

	static QueueHelperHTTPClient getInstance(String apikey) throws IOException
	{
		return null;
	}


	void setAPikey(String apikey);

	public String getCustomerID(String name) throws IOException;

	public boolean markCustomer(int option, String name) throws IOException;

	public List<String[]> readCSV(List<String[]> csv) throws IOException;//Please pay close attention to how the queue object expects the customer name/id and so forth. A small quirk is it expect cooldown status to be present in the "notes" part of the customer

	public NavigationButton getNavButton();

	public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException;

}
