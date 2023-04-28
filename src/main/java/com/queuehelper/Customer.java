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

public class Customer
{
	private String CustomerName;

	private String CustomerStatus;

	private String CustomerPriority;

	private String CustomerID;

	private String CustomerNotes;

	private String CustomerItem;

	public Customer(String name, String id, String priority, String CustomerStatus, String CustomerNotes, String CustomerItem){
		this.CustomerName = name;
		this.CustomerID = id;
		this.CustomerPriority = priority;
		this.CustomerStatus = CustomerStatus;
		this.CustomerNotes = CustomerNotes;
		this.CustomerItem = CustomerItem;
	}

	public Customer(String id, String priority, String priority1, String customerNotes, String customerItem)
	{
	}

	public String getName(){
		return this.CustomerName;
	}
	public String getPriority(){
		return this.CustomerPriority;
	}
	public String getID(){
		return this.CustomerID;
	}
	public String getStatus(){
		return this.CustomerStatus;
	}
	public String getItem(){
		return this.CustomerItem;
	}
	public String getNotes() {
		return this.CustomerNotes;
	}

	public void setName(String name){
		this.CustomerName = name;
	}
	public void setPriority(String priority){
		this.CustomerPriority = priority;
	}
	public void setID(String id){
		this.CustomerID = id;
	}
	public void setStatus(String status){
		this.CustomerStatus = status;
	}
	public void setItem(String item){
		this.CustomerItem = item;
	}
	public void setNotes(String notes) {
		this.CustomerNotes = notes;
	}


}
