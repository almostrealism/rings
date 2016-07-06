/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.almostrealism.flow.db.Client;


public class HttpCommandServer implements Runnable {
	private static SimpleDateFormat format =
		new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	private ServerSocket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader br;
	private PrintStream ps;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]));
		HttpCommandServer s = new HttpCommandServer(socket);
		
		Client c = Client.getCurrentClient();
		ThreadGroup g = null;
		if (c != null) g = c.getServer().getThreadGroup();
		Thread t = new Thread(g, s, "HttpCommandServer");
		t.start();
		System.out.println("HttpCommandServer: Started");
	}
	
	public HttpCommandServer(ServerSocket socket) {
		this.socket = socket;
	}
	
	public void run() {
		while (true) {
			try {
				Socket connection = this.socket.accept();
				System.out.println("HttpCommandServer: Accepted connection...");
				
				this.in = connection.getInputStream();
				this.out = connection.getOutputStream();
				System.out.println("HttpCommandServer: Got IO streams...");
				
				this.br = new BufferedReader(new InputStreamReader(this.in));
				this.ps = new PrintStream(this.out, false, "US-ASCII");
				System.out.println("HttpCommandServer: Constructed input buffer and print stream...");
				
				String command = null;
				
				w: while(true) {
					String s = this.br.readLine();
					
					s = s.trim();
					
					if (s == null || s.equals("")) {
						break w;
					} else if (s.startsWith("GET /")) {
						int p = s.indexOf("?") + 1;
						int q = s.indexOf(" HTTP/");
						
						command = s.substring(p, q);
						command = command.replaceAll("%20", " ");
					}
				}
				
				if (Message.verbose)
					System.out.println("HttpCommandServer: Recieved " + command);
				
				if (command != null) {
					String date = HttpCommandServer.format.format(new Date());
					
					this.ps.println("HTTP/1.0 200 OK");
					this.ps.println("Date: " + date);
					this.ps.println("Content-Type: text/html");
					this.ps.println("Connection: Close");
					this.ps.println();
					this.ps.println(NetworkClient.runCommand(command, ps));
					
					if (Message.verbose)
						System.out.println("HttpCommandServer: Sent HTTP header and output.");
				} else {
					System.out.println("HttpCommandServer: Recieved null command");
				}
				
				this.in.close();
				this.out.close();
				connection.close();
			} catch (IOException ioe) {
				System.out.println("HttpCommandServer: IO error accepting connection (" +
									ioe.getMessage() + ")");
			} catch (Exception e) {
				System.out.println("HttpCommandServer: " + e);
			}
		}
	}
}
