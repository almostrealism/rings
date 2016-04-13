package com.wifi_cell.voip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Datagram;
import javax.microedition.io.Connector;
import javax.microedition.media.Control;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.DatagramConnection;
import javax.microedition.media.protocol.SourceStream;
import javax.microedition.media.protocol.ContentDescriptor;

public class RTPSourceStream implements SourceStream {

	private RTSPProtocolHandler handler;

	private InputStream is;
	private OutputStream os;

	private DatagramConnection socket;

	public RTPSourceStream(String address) throws IOException {

		// create the protocol handler and set it up so that the
		// application is ready to read data

		// create a socketconnection to the remote host
		// (in this case I have set it up so that its localhost, you can
		// change it to wherever your server resides)
		SocketConnection sc =
		  (SocketConnection)Connector.open("socket://localhost:554");

		// open the input and output streams
		is = sc.openInputStream();
		os = sc.openOutputStream();

		// and initialize the handler
		handler = new RTSPProtocolHandler(address, is, os);

		// send the basic signals to get it ready
		handler.doDescribe();
		handler.doSetup();
	}

	public void start() throws IOException {
	  handler.doPlay();
	}

	public void close() throws IOException {

		if(handler != null) handler.doTeardown();

		is.close();
		os.close();
	}

	public int read(byte[] buffer, int offset, int length)
	  throws IOException {

		 // create a byte array which will be used to read the datagram
		 byte[] fullPkt = new byte[length];

		 // the new Datagram
		 Datagram packet = socket.newDatagram(fullPkt, length);

		 // receive it
		 socket.receive(packet);

		 // extract the actual RTP Packet's media data in the requested buffer
	   RTPPacket rtpPacket = getRTPPacket(packet, packet.getData());
	   buffer = rtpPacket.getData();

	   // debug
	   System.err.println(rtpPacket + " with media length: " + buffer.length);

		 // and return its length
		 return buffer.length;
	}

	// extracts the RTP packet from each datagram packet received
	private RTPPacket getRTPPacket(Datagram packet, byte[] buf) {

	  // SSRC
	  long SSRC = 0;

		// the payload type
		byte PT = 0;

	  // the time stamp
		int timeStamp = 0;

		// the sequence number of this packet
		short seqNo = 0;


		// see http://www.networksorcery.com/enp/protocol/rtp.htm
		// for detailed description of the packet and its data
		PT =
		  (byte)((buf[1] & 0xff) & 0x7f);

		seqNo =
		  (short)((buf[2] << 8) | ( buf[3] & 0xff));

		timeStamp =
		  (((buf[4] & 0xff) << 24) | ((buf[5] & 0xff) << 16) |
		    ((buf[6] & 0xff) << 8) | (buf[7] & 0xff)) ;

		SSRC =
		  (((buf[8] & 0xff) << 24) | ((buf[9] & 0xff) << 16) |
		    ((buf[10] & 0xff) << 8) | (buf[11] & 0xff));


		// create an RTPPacket based on these values
		RTPPacket rtpPkt = new RTPPacket();

		// the sequence number
		rtpPkt.setSequenceNumber(seqNo);

		// the timestamp
		rtpPkt.setTimeStamp(timeStamp);

		// the SSRC
		rtpPkt.setSSRC(SSRC);

		// the payload type
		rtpPkt.setPayloadType(PT);

		// the actual payload (the media data) is after the 12 byte header
		// which is constant
		byte payload[] = new byte [packet.getLength() - 12];

		for(int i=0; i < payload.length; i++) payload [i] = buf[i+12];

		// set the payload on the RTP Packet
		rtpPkt.setData(payload);

		// and return the payload
		return rtpPkt;

	}

	public long seek(long where) throws IOException {
	 throw new IOException("cannot seek");
	}

	public long tell() { return -1; }

	public int getSeekType() { return NOT_SEEKABLE;	}

	public Control[] getControls() { return null; }

	public Control getControl(String controlType) { return null; }

	public long getContentLength() { return -1;	}

	public int getTransferSize() { return -1;	}

	public ContentDescriptor getContentDescriptor() {
		return new ContentDescriptor("audio/rtp");
	}
}