/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

/**
 * A Message object is used to send a message using a Proxy.
 * 
 * @author Mike Murray
 */
public class Message implements Externalizable {
	public static final int Job = 1;
	public static final int StringMessage = 2;
	public static final int ConnectionRequest = 4;
	public static final int ConnectionConfirmation = 8;
	public static final int ServerStatusQuery = 10;
	public static final int ServerStatus = 11;
	public static final int ResourceRequest = 12;
	public static final int ResourceUri = 13;
	public static final int DistributedResourceUri = 14;
	public static final int DistributedResourceInvalidate = 15;
	public static final int Task = 16;
	public static final int Kill = 32;
	public static final int Ping = 64;
	
	public static boolean verbose = false;
	public static boolean dverbose = false;
	public static boolean sverbose = false;
	
	private transient int type;
	private transient int sender = -1, reciever = -1;
	
	private transient NodeProxy proxy;
	private transient Node node;
	private transient String data;
	
	private transient boolean local, bypass;
	
	public Message() {
		assert false : "Message constructed with no NodeProxy";
		new Exception().printStackTrace();
	}
	
	/**
	 * Constructs a Message object that will be treated as a message
	 * that was recieved from a remote node.
	 */
	public Message(NodeProxy p) {
		this.local = false;
		this.proxy = p;
	}
	
	/**
	 * Constructs a new Message object of the specified type.
	 * 
	 * @param type  Type of message.
	 * @param id  Id of sending node.
	 * @throws IOException  Never thrown.
	 */
	public Message(int type, int id) throws IOException {
		this(type, id, null);
	}
	
	/**
	 * Constructs a new Message object using the specified IO streams and type code.
	 * 
	 * @param type  Type of message.
	 * @param id  Id of sending node.
	 * @throws IOException  If an IOException occurs while getting IO streams.
	 */
	public Message(int type, int id, NodeProxy p) throws IOException {
		this.type = type;
		this.sender = id;
		
		this.proxy = p;
		
		this.local = true;
	}
	
	public void setQueueBypass(boolean bypass) { this.bypass = bypass; }
	
	/**
	 * Sets the Job to be sent with this message, if this message is the correct type.
	 * 
	 * @param j  The Job object to be encoded and transmitted.
	 */
	public void setJob(Job j) { if (this.type == Message.Job) this.data = j.encode(); }
	
	/**
	 * Sets the String to be sent with this message, if this message is the correct type.
	 * 
	 * @param s  The String to be transmitted.
	 */
	public void setString(String s) { this.data = s; }
	
	/**
	 * Sets the local node to be used as an endpoint if this message is a connection request.
	 * 
	 * @param node  Node object to connect with.
	 */
	public void setLocalNode(Node node) { if (this.local) this.node = node; }
	
	/**
	 * @return  The integer id of the node that is the sender of this message.
	 */
	public int getSender() { return this.sender; }
	
	/**
	 * Sets the integer id of the node that is the reciever of this message.
	 * 
	 * @param id  Integer id to use.
	 */
	public void setReciever(int id) { this.reciever = id; }
	
	/**
	 * @return  The integer id of the node that is the reciever of this message.
	 */
	public int getReciever() { return this.reciever; }
	
	/**
	 * @return  The integer type code for this message.
	 */
	public int getType() { return this.type; }
	
	/**
	 * @return  The data stored by this message.
	 */
	public String getData() { return this.data; }
	
	/**
	 * @return  True if this message was produced by a local node, false if it was sent by a remote node.
	 */
	public boolean isLocalMessage() { return this.local; }
	
	/**
	 * @return  The NodeProxy object stored by this Message object.
	 */
	public NodeProxy getNodeProxy() { return this.proxy; }
	
	/**
	 * Sends the data for this message and waits for a response, which is returned as an Object.
	 * 
	 * @param id  Unique id of remote node.
	 * @throws IOException  If an IO error occurs while sending the message.
	 * @return  Response to message.
	 */
	public Object send(int id) throws IOException {
		if (proxy == null) return null;
		
		if (this.type == Message.Job && this.data == null) {
			if (Message.verbose)
				System.out.println("Message: Null job not sent.");
			return null;
		}
		
		if (this.bypass)
			this.proxy.writeObject(this, id, false);
		else
			this.proxy.writeObject(this, id);
			
		if (this.type == Message.ConnectionRequest) {
			Message m = (Message) this.proxy.waitFor(this.sender, 10000);
			
			if (m != null && m.getSender() >= 0) {
				System.out.println("Message: Constructing connection...");
				return new Connection(this.node, this.proxy, m.getSender());
			}
		} else if (this.type == Message.ConnectionConfirmation && this.data == null) {
			Message m = (Message) this.proxy.waitFor(this.sender, 10000);
			
			if (m == null)
				return Boolean.valueOf(false);
			else
				return Boolean.valueOf(m.getData());
		} else if (this.type == Message.ServerStatusQuery) {
			Message m = (Message) this.proxy.waitForMessage(Message.ServerStatus, null, 10000);
			
			if (m == null) {
				return null;
			} else if (m.getData().startsWith("peers:")) {
				String ps = m.getData();
				ps = ps.substring(ps.indexOf(":") + 1);
				String peers[] = ps.split(",");
				ArrayList l = new ArrayList(peers.length);
				
				for (int i = 0; i < peers.length; i++) {
					int index = peers[i].indexOf("/");
					if (index >= 0)
						peers[i] = peers[i].substring(index + 1);
					
					l.add(peers[i]);
				}
				
				return l;
			}
		} else if (this.type == Message.ResourceRequest) {
			Message m = (Message) this.proxy.waitForMessage(Message.ResourceUri, null, 10000);
			
			if (m == null)
				return null;
			else
				return m.getData();
		}
		
		return null;
	}
	
	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		if (Message.verbose) System.out.println("Write " + this.toString());
		
		out.writeInt(this.sender);
		out.writeInt(this.reciever);
		out.writeInt(this.type);
		out.writeObject(this.data);
	}
	
	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.sender = in.readInt();
		this.reciever = in.readInt();
		this.type = in.readInt();
		
		this.data = (String) in.readObject();
		
		if (Message.verbose) System.out.println("Read " + this.toString());
		
		this.node = null;
		this.local = false;
	}
	
	public void setBytes(byte b[]) {
		this.sender = b[0];
		this.reciever = b[1];
		this.type = b[2];
		
		if (b.length > 3)
			this.data = new String(b, 3, b.length - 3);
		
		if (Message.verbose)
			System.out.println("Read " + b.length + " bytes " + this.toString());
		
		this.node = null;
		this.local = false;
	}
	
	public byte[] getBytes() {
		byte db[] = new byte[0];
		if (this.data != null) db = this.data.getBytes();
		byte b[] = new byte[3 + db.length];
		
		b[0] = (byte) this.sender;
		b[1] = (byte) this.reciever;
		b[2] = (byte) this.type;
		
		for (int i = 0; i < db.length; i++) b[3 + i] = db[i];
		
		if (Message.verbose)
			System.out.println("Write " + b.length + " bytes " + this.toString());
		
		return b;
	}
	
	public String toString() {
		String t = null;
		
		if (this.type == Message.Job)
			t = "Job";
		else if (this.type == Message.StringMessage)
			t = "String";
		else if (this.type == Message.ConnectionRequest)
			t = "Connection";
		else if (this.type == Message.ConnectionConfirmation)
			t = "Confirm";
		else if (this.type == Message.ResourceRequest)
			t = "RequestUri";
		else if (this.type == Message.ResourceUri)
			t = "ResourceUri";
		else if (this.type == Message.DistributedResourceUri)
			t = "DistResourceUri";
		else if (this.type == Message.DistributedResourceInvalidate)
			t = "DistResourceDel";
		else if (this.type == Message.Task)
			t = "Task";
		else if (this.type == Message.Kill)
			t = "Kill";
		else if (this.type == Message.Ping)
			t = "Ping";
		else
			t = String.valueOf(this.type);
		
		String s = this.data;
		if (s != null && s.length() > 100) s = s.substring(0, 100) + "...";
		
		return "Message: "+ this.sender + " " + this.reciever + " " + t + " " + s;
	}
}
