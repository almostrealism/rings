package com.wifi_cell.voip;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;

public class WifiCellMIDlet extends MIDlet implements CommandListener {
	public static final String server = "datagram://wifi-cell.net:5060"
	
	private Form form;
	private TextField phoneField;
	
	private DatagramConnection dataCon;

	public WifiCellMIDlet() {
		form = new Form("VoIP Call");
		phoneField = new TextField("Phone Number", "", 12, TextField.NUMERIC);
		form.append(phoneField);

		form.addCommand(new Command("EXIT", Command.EXIT, 2));
		form.addCommand(new Command("HELP", Command.HELP, 2));
		form.addCommand(new Command("OK", Command.OK, 1));

		// set itself as the command listener
		form.setCommandListener(this);
	}


	protected void destroyApp(boolean unc) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		Display display = Display.getDisplay(this);
		display.setCurrent(form);
	}


	public void commandAction(Command c, Displayable arg1) {
		if (c.getCommandType() == Command.OK) {
			this.startCall();
		}
	}
	
	public void startCall() {
		this.dataCon = Connector.open(server);
		
		SIPUSH
	}
	
	protected Datagram readPacket(int length) {
		byte data[] = new byte[length];
		Datagram pack = dataCon.newDatagram(data, length);
		dataCon.receive(pack);
		return pack;
	}
	
	protected 
}
