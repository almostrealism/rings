package com.almostrealism.metamerise.dmx;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;

/**
 * The DMXClient class provides access to the DMX web service.
 * 
 * @author  Michael Murray
 */
public class DMXClient {
	private static DMXClient singleton = new DMXClient();
	
	public DMXClient() { }
	
	/**
	 * Makes a request to the DMX server to execute the specified command.
	 */
	public void postCommand(String command) throws IOException {
		System.out.println("DMX: " + command);
		URL request = new URL(DMXControlConstants.formatRequest(command));
		InputStream in = request.openStream();
		
		// Wait till we are "done"
		while (in.available() > 0) in.read();
	}
	
	/**
	 * Issues a command to the DMX server to set the specified channel
	 * to the specified value.
	 */
	public void setChannel(int channel, String value) {
		int tries = 0;
		
		while (true) {
			try {
				tries++;
				postCommand(channel + "@" + value);
				return;
			} catch (ConnectException c) {
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// Try again
				if (tries > 1) return;
			} catch (IOException ioe) {
				System.err.println("DMX: " + ioe);
				return;
			}
		}
	}
	
	/**
	 * Issues a collection of commands.
	 */
	public void setChannels(String channelValues[][]) {
		if (channelValues.length <= 0) return;
		
		StringBuffer cmd = new StringBuffer();
		
		for (int i = 0; i < channelValues.length; i++) {
			setChannel(Integer.parseInt(channelValues[i][0]), channelValues[i][1]);
		}
//		
//		try {
//			postCommand(cmd.toString());
//		} catch (IOException ioe) {
//			System.err.println("DMX: " + ioe);
//		}
	}
	
	/**
	 * Returns the static instance of the DMX client.
	 */
	public static DMXClient getDefaultDMXClient() { return singleton; }
}
