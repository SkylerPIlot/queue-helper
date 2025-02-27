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
import java.util.List;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.ui.NavigationButton;

//To create your own implementation, change which implementation is called by the Queue object. To make sure data is
	//"Correct" look how the queue class handles the IO data especially how it creates the list of customers

public interface QueueHelperHTTPClient
{

	static QueueHelperHTTPClient getInstance(String apikey) throws IOException
	{
		return null;
	}


	void setAPikey(String apikey);

	public String getCustomerID(String name) throws IOException;

	public boolean markCustomer(int option, String name, String rankname) throws IOException;

	public List<String[]> readCSV(List<String[]> csv) throws IOException;//Please pay close attention to how the queue object expects the customer name/id and so forth. A small quirk is it expect cooldown status to be present in the "notes" part of the customer

	public NavigationButton getNavButton();

	public boolean updateQueuebackend(StringBuilder urlList, String name) throws IOException;

	public boolean addCustomer(String itemName,String priority, String custName, String addedBy) throws IOException;

	public boolean sendChatMsgDiscord(ChatMessage chatmessage) throws IOException;

	public boolean sendRoundTimeServer(String main, String collector, String healer, String leech, String Defender, int time, int premiumType, String item
										,int attpts, int defpts, int healpts, int collpts, int eggsCollected, int hpHealed, int wrongAtts, String leechrole);
}
