package com.wifi_cell.voip;

import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;
import javax.microedition.media.Manager;

public class StreamingMIDlet extends MIDlet {

	public void startApp() {

		try {

		  // create Player instance, realize it and then try to start it
		  Player player =
		    Manager.createPlayer(
					new StreamingDataSource(
						"rtsp://localhost:554/sample_100kbit.mp4"));

			player.realize();

			player.start();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}
}
