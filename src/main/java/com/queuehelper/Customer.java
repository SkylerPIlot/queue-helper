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
