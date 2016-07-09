/**
 * Copyright 2008 RoxStream Media
 */

package com.almostrealism.apple;

import java.io.FileNotFoundException;

import net.sf.jrings.apple.AppleSettings;
import net.sf.jrings.apple.RingsStatus;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSMenuItem;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSStatusBar;
import com.apple.cocoa.application.NSStatusItem;
import com.apple.cocoa.application._NSObsoleteMenuItemProtocol;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

public class RoxStatus extends NSMenu {
	private RingsStatus rings;

	public static void main(String args[]) throws FileNotFoundException {
		if (AppleSettings.application == null)
			AppleSettings.application = NSApplication.sharedApplication();

		RoxStatus s = new RoxStatus();
		
		NSApplication.sharedApplication().setServicesProvider(s);
		AppleSettings.application.setMenu(s);
		AppleSettings.application.run();
	}
	
	public void applicationDidFinishLaunching (NSNotification notification) {
		
		System.out.println("RoxStatus: app launched.");
	}

	public boolean applicationShouldTerminate (NSApplication app) {
		return false;
	}

	public boolean applicationOpenFile (NSApplication app, String filename) {
		return false;

	}

	public boolean applicationOpenTempFile (NSApplication app, String filename) {
		return false;
	}

	public boolean applicationOpenUntitledFile (NSApplication app) {
		return false;
	}

	public boolean applicationPrintFile(NSApplication app, String filename) {
		return false;
	}

	public void createNew(Object sender) {
	}

	public void open(Object sender) {
	}

	public void saveAll(Object sender) {
	}

	// Services support

	String servicesOpenFile(NSPasteboard pboard, String data) {
		return null;	// No need to report an error string...
	}

	String servicesOpenSelection(NSPasteboard pboard, String data) {
		return data;
	}

	// Scripting support.

	public NSArray orderedDocuments() {
		return null;
	}

	public boolean applicationDelegateHandlesKey(NSApplication application, String key) {
		return false;
	}

	public boolean validateMenuItem(_NSObsoleteMenuItemProtocol aCell) {
		return false;
	}

	public RoxStatus() throws FileNotFoundException {
		super("RoxStream");
		
		NSSelector selector = new NSSelector("statusMenuClick", new Class[] {});
		super.addItem(new NSMenuItem("Test", selector, "Test"));
		
		this.rings = new RingsStatus();
		activateStatusMenu();
	}

	public void activateStatusMenu() {
		NSMenu menu = new NSMenu("Rox");
		NSStatusBar bar = NSStatusBar.systemStatusBar();
		NSStatusItem item = bar.statusItem(NSStatusBar.VariableStatusItemLength);
		item.setTarget(item);
		item.setTitle("Rox");
		item.setHighlightMode(true);
		item.setMenu(menu);
		item.setEnabled(true);

		NSMenu ringsMenu = new NSMenu("Rings");
//		ringsMenu.setSubmenu(menu);

		NSMenuItem ringsItems[] = this.rings.getMenuItems();

		for (int i = 0; i < ringsItems.length; i++) ringsItems[i].setSubmenu(menu);
	}

	public void statusMenuClick() {
		System.out.println("StatusMenuClick");
	}
}