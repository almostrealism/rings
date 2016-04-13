package net.sf.jrings.apple;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSNetService;

public class AppleSettings {
	public static NSApplication application;
	
	public static int netServicePort = 6769;
	public static NSNetService netService;
	
	public static void initNetService() {
		if (AppleSettings.application == null)
			AppleSettings.application = new NSApplication();
		
		netService = new NSNetService("", "_rings._tcp", String.valueOf(netServicePort));
	}
}
