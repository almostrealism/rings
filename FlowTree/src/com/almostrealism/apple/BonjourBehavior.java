/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.apple;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

import com.almostrealism.flow.Server;
import com.almostrealism.flow.ServerBehavior;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSNetService;
import com.apple.cocoa.foundation.NSNetServiceBrowser;

public class BonjourBehavior implements ServerBehavior {
	private Server server;
	
	public void behave(Server s, PrintStream out) {
		this.server = s;
		
		if (AppleSettings.application == null)
			AppleSettings.application = new NSApplication();
		
		if (AppleSettings.netService == null) {
			AppleSettings.initNetService();
		}
		
		NSNetServiceBrowser browser = new NSNetServiceBrowser();
		browser.setDelegate(this);
		browser.searchForServicesOfType("_rings._tcp", "");
		
		this.server = null;
	}
	
	public void netServiceBrowserWillSearch() {
		
	}
	
	public void netServiceBrowserDidStopSearch() {
		
	}
	
	public void didFindService(NSNetServiceBrowser browser, NSNetService service, boolean more) {
		if (service != null) {
			try {
				this.server.open(service.hostName());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
