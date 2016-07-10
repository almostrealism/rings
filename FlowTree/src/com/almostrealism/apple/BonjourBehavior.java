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
