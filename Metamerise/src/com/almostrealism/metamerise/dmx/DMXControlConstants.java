package com.almostrealism.metamerise.dmx;

import java.net.URLEncoder;

/**
 * The DMXControlConstants class contains the configuration for the DMX
 * control web service which is used to control the lighting rig.
 * 
 * @author  Michael Murray
 */
public class DMXControlConstants {
	// http://10.0.1.4:10024/dmx?cmd=1%2F4%400&keyEnter=Enter
	
	public static String dmxServerAddress = "http://10.0.1.4:10024/";
	public static String dmxCommandPrefix = "dmx?cmd=";
	public static String dmxCommandSuffix = "&keyEnter=Enter";
	
	/**
	 * Creates a URL string for the specified DMX web service command.
	 */
	public static String formatRequest(String command) {
		return dmxServerAddress + dmxCommandPrefix + escape(command) + dmxCommandSuffix;
	}
	
	/**
	 * Escapes special characters in the specified string.
	 */
	public static String escape(String s) { return URLEncoder.encode(s); }
}
