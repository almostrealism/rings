package com.wifi_cell.voip;

import java.util.Vector;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RTSPProtocolHandler {

	// the address of the media file as an rtsp://... String
	private String address;

	// the inputstream to receive response from the server
	private InputStream is;

	// the outputstream to write to the server
	private OutputStream os;

	// the incrementing sequence number for each request
	// sent by the client
	private static int CSeq = 1;

	// the session id sent by the server after an initial setup
	private String sessionId;

	// the number of tracks in a media file
	private Vector tracks = new Vector(2);

	// flags to indicate the status of a session
	private boolean described, setup, playing;
	private boolean stopped = true;

	// constants
	private static final String CRLF = "\r\n";
	private static final String VERSION = "rtsp/1.0";
	private static final String TRACK_LINE = "a=control:trackID=";
	private static final String TRANSPORT_DATA =
	  "TRANSPORT: UDP;unicast;client_port=8080-8081";
	private static final String RTSP_OK = "RTSP/1.0 200 OK";

	// base constructor, takes the media address, input and output streams
	public RTSPProtocolHandler(
		String address, InputStream is, OutputStream os) {

		this.address = address;
		this.is = is;
		this.os = os;
	}

	// creates, sends and parses a DESCRIBE client request
	public void doDescribe() throws IOException {

		// if already described, return
		if(described) return;

		// create the base command
		String baseCommand = getBaseCommand("DESCRIBE " + address);

		// execute it and read the response
		String response = doCommand(baseCommand);

		// the response will contain track information, amongst other things
		parseTrackInformation(response);

		// set flag
		described = true;
	}

	// creates, sends and parses a SETUP client request
	public void doSetup() throws IOException {

		// if not described
		if(!described) throw new IOException("Not Described!");

		// create the base command for the first SETUP track
		String baseCommand =
		  getBaseCommand(
				"SETUP " + address + "/trackID=" + tracks.elementAt(0));

		// add the static transport data
		baseCommand += CRLF + TRANSPORT_DATA;

		// read response
		String response = doCommand(baseCommand);

		// parse it for session information
		parseSessionInfo(response);

		// if session information cannot be parsed, it is an error
		if(sessionId == null)
		  throw new IOException("Could not find session info");

		// now, send SETUP commands for each of the tracks
		int cntOfTracks = tracks.size();
		for(int i = 1; i < cntOfTracks; i++) {
			baseCommand =
				getBaseCommand(
					"SETUP " + address + "/trackID=" + tracks.elementAt(i));
			baseCommand += CRLF + "Session: " + sessionId + CRLF + TRANSPORT_DATA;
			doCommand(baseCommand);
		}

		// this is now setup
		setup = true;
	}

	// issues a PLAY command
	public void doPlay() throws IOException {

		// must be first setup
		if(!setup) throw new IOException("Not Setup!");

		// create base command
		String baseCommand = getBaseCommand("PLAY " + address);

		// add session information
		baseCommand += CRLF + "Session: " + sessionId;

		// execute it
		doCommand(baseCommand);

		// set flags
		playing = true;
		stopped = false;
	}

	// issues a PAUSE command
	public void doPause() throws IOException {

		// if it is not playing, do nothing
		if(!playing) return;

		// create base command
		String baseCommand = getBaseCommand("PAUSE " + address);

		// add session information
		baseCommand += CRLF + "Session: " + sessionId;

		// execute it
		doCommand(baseCommand);

		// set flags
		stopped = true;
		playing = false;
	}

	// issues a TEARDOWN command
	public void doTeardown() throws IOException {

		// if not setup, nothing to teardown
		if(!setup) return;

		// create base command
		String baseCommand = getBaseCommand("TEARDOWN " + address);

		// add session information
		baseCommand += CRLF + "Session: " + sessionId;

		// execute it
		doCommand(baseCommand);

		// set flags
		described = setup = playing = false;
		stopped = true;
	}

	// this method is a convenince method to put a RTSP command together
	private String getBaseCommand(String command) {

		return(
			command +
			" " +
			VERSION + // version
			CRLF +
			"CSeq: " + (CSeq++) // incrementing sequence
		);
	}

	// executes a command and receives response from server
	private String doCommand(String fullCommand) throws IOException {

		// to read the response from the server
		byte[] buffer = new byte[2048];

		// debug
		System.err.println(" ====== CLIENT REQUEST ====== ");
		System.err.println(fullCommand + CRLF + CRLF);
		System.err.println(" ============================ ");

		// send a command
		os.write((fullCommand + CRLF + CRLF).getBytes());

		// read response
		int length = is.read(buffer);

		String response = new String(buffer, 0, length);

		// empty the buffer
		buffer = null;

		// if the response doesn't start with an all clear
		if(!response.startsWith(RTSP_OK))
		  throw new IOException("Server returned invalid code: " + response);

		// debug
		System.err.println(" ====== SERVER RESPONSE ====== ");
		System.err.println(response.trim());
		System.err.println(" =============================");

		return response;
	}

	// convenience method to parse a server response to DESCRIBE command
	// for track information
	private void parseTrackInformation(String response) {

		String localRef = response;
		String trackId = "";
		int index = localRef.indexOf(TRACK_LINE);

		// iterate through the response to find all instances of the
		// TRACK_LINE, which indicates all the tracks. Add all the
		// track id's to the tracks vector
		while(index != -1) {
			int baseIdx = index + TRACK_LINE.length();
			trackId = localRef.substring(baseIdx, baseIdx + 1);
			localRef = localRef.substring(baseIdx + 1, localRef.length());
			index = localRef.indexOf(TRACK_LINE);
			tracks.addElement(trackId);
		}

	}

	// find out the session information from the first SETUP command
	private void parseSessionInfo(String response) {

		sessionId =
		  response.substring(
				response.indexOf("Session: ") + "Session: ".length(),
				response.indexOf("Date:")).trim();

	}


}