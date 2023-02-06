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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("BAS")
public interface BASConfig extends Config {
  @ConfigItem(keyName = "autoUpdateQueue", name = "Queue Auto Updater", description = "Automatically updates the BAS Queue", position = 1)
  default boolean autoUpdateQueue() {
    return true;
  }
  
  @ConfigItem(keyName = "queueName", name = "Queue Sheet Name", description = "The name that you would like the queue to recognise you as. If not set it will use the currently logged in username.", position = 2)
  default String queueName() {
    return "";
  }

	@ConfigItem(
		keyName = "fontsize",
		name = "Note Font Size",
		description = "changes the font size in the notes",
		position = 3
	)
	default int fontSize()
	{
		return 16;
	}


	@ConfigItem(
		keyName = "APIKEY",
		name = "KEY",
		description = "Please place your api key here",
		position = 4
	)
	default String apikey()
	{
		return "Paste your key here";
	}

}
