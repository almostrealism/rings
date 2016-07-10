/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.interfaces;

import java.net.UnknownHostException;

import com.almostrealism.flow.NetworkClient;
import com.echomine.common.SendMessageFailedException;
import com.echomine.jabber.JID;
import com.echomine.jabber.Jabber;
import com.echomine.jabber.JabberChatMessage;
import com.echomine.jabber.JabberChatService;
import com.echomine.jabber.JabberCode;
import com.echomine.jabber.JabberContext;
import com.echomine.jabber.JabberMessageEvent;
import com.echomine.jabber.JabberMessageException;
import com.echomine.jabber.JabberMessageListener;
import com.echomine.jabber.JabberSession;
import com.echomine.net.ConnectionFailedException;

public class JabberInterface implements JabberMessageListener {
	private JabberSession session;
	private JabberChatService service;
	
	/**
	 * @param args
	 */
	public static void main(String args[]) {
		String user = "default";
		String password = "default";
		String host = "localhost";
		int port = 5222;
		
		if (args.length > 0) user = args[0];
		if (args.length > 1) password = args[1];
		if (args.length > 2) host = args[2];
		if (args.length > 3) port = Integer.parseInt(args[3]);
		
		System.out.println("Initializing Jabber Interface...");
		JabberInterface jabber = new JabberInterface(user, password, host, port);
		System.out.println("Connecting...");
		
		try {
			jabber.start(host, port);
		} catch (ConnectionFailedException e) {
			System.out.println("JabberInterface: Connection failed (" + e.getMessage() + ")");
		} catch (UnknownHostException e) {
			System.out.println("JabberInterface: Unknown host " + host);
		} catch (JabberMessageException e) {
			System.out.println("JabberInterface: " + e);
		} catch (SendMessageFailedException e) {
			System.out.println("JabberInterface: " + e);
		}
	}
	
	public JabberInterface(String user, String password, String host, int port) {
		Jabber jabber = new Jabber();
		JabberContext ctx = new JabberContext(user, password, host);
		this.session = jabber.createSession(ctx);
		this.service = this.session.getChatService();
		this.session.addMessageListener(this);
	}
	
	public void start(String host, int port) throws ConnectionFailedException,
												UnknownHostException,
												JabberMessageException,
												SendMessageFailedException {
		System.out.println("JabberInterface: Connecting to " + host + ":" + port + "...");
		this.session.connect(host, port);
		System.out.println("JabberInterface: Logging in...");
		this.session.getUserService().login();
		System.out.println("JabberInterface: Started.");
	}

	public void messageReceived(JabberMessageEvent event) {
		if (event.getMessageType() != JabberCode.MSG_CHAT) return;
		
		JabberChatMessage msg = (JabberChatMessage) event.getMessage();
		final JID from = msg.getFrom();
		final String id = msg.getThreadID();
		final String command = msg.getBody();
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				String reply = NetworkClient.runCommand(command, null);
				try {
					JabberInterface.this.service.replyToPrivateMessage(from, id, reply, false);
				} catch (SendMessageFailedException e) {
					System.out.println("JabberInterface: " + e);
				}
			}
		});
		
		t.start();
	}
}
