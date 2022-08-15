package com.queuehelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("BAS")
public interface BASConfig extends Config {
  @ConfigItem(keyName = "autoUpdateQueue", name = "Queue Auto Updater", description = "Automatically updates the BAS Queue", position = 1)
  default boolean autoUpdateQueue() {
    return true;
  }
  
  @ConfigItem(keyName = "markCustomerOptions", name = "Mark Customer Options", description = "Adds options to mark customers", position = 3)
  default boolean markCustomerOptions() {
    return false;
  }
  
  @ConfigItem(keyName = "getNextCustomer", name = "Get Next Customer Option", description = "Button to announce the next customer (replaces Clan Setup button)", position = 4)
  default boolean getNextCustomer() {
    return true;
  }
  
  @ConfigItem(keyName = "queueName", name = "Queue Sheet Name", description = "The name that you would like the queue to recognise you as. If not set it will use the currently logged in username.", position = 5)
  default String queueName() {
    return "";
  }
  
  @ConfigItem(keyName = "addToQueue", name = "Shift Add To Queue Options", description = "Hold shift to view more options that allow adding customers directly to the queue", position = 6)
  default boolean addToQueue() {
    return false;
  }
  
  @ConfigItem(keyName = "torsoOptions", name = "Options - Torso", description = "Show options to add Torso when \"Shift add to queue options\" is enabled", position = 7)
  default boolean torsoOptions() {
    return false;
  }
  
  @ConfigItem(keyName = "hatOptions", name = "Options - Hat", description = "Show options to add Hat when \"Shift add to queue options\" is enabled", position = 8)
  default boolean hatOptions() {
    return false;
  }
  
  @ConfigItem(keyName = "qkOptions", name = "Options - Queen Kill", description = "Show options to add Queen Kill when \"Shift add to queue options\" is enabled", position = 9)
  default boolean qkOptions() {
    return false;
  }
  
  @ConfigItem(keyName = "OneROptions", name = "Options - One Round", description = "Show options to add One Round - Points when \"Shift add to queue options\" is enabled", position = 10)
  default boolean OneROptions() {
    return false;
  }
  
  @ConfigItem(keyName = "Lvl5Options", name = "Options - Level 5 Roles", description = "Show options to add Level 5 Roles when \"Shift add to queue options\" is enabled", position = 11)
  default boolean Lvl5Options() {
    return false;
  }
	@ConfigItem(
		keyName = "APIKEY",
		name = "KEY",
		description = "Please place your api key here",
		position = 13
	)
	default String apikey()
	{
		return "";
	}
}
