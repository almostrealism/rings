/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package net.sf.j3d.run;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.almostrealism.flow.Message;
import com.almostrealism.flow.NetworkClient;
import com.almostrealism.flow.db.Client;
import com.almostrealism.flow.db.OutputServer;

/**
 * The RayTracingServer class provides a main method that starts a DB Server
 * and a Network Client using a properties file specified by the command line
 * argument.
 * 
 * @author Mike Murray
 */
public class RayTracingServer {
	public static void main(String[] args) {
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(args[0]));
			
			String msgv = p.getProperty("network.msg.verbose", "false");
			Message.verbose = Boolean.parseBoolean(msgv);
			
			RayTracingServer server = new RayTracingServer(p);
		} catch (Exception e) {
			System.out.println("RayTracingServer: " + e.getMessage());
		}
	}
	
	public RayTracingServer(Properties p) throws IOException {
		OutputServer s = new OutputServer(p);
		
		System.out.println("DB Server started");
		
		String user = p.getProperty("client.user");
		String passwd = p.getProperty("client.passwd");
		
		if (user == null || passwd == null) {
			System.out.println("Missing user and/or password in config file.");
			System.exit(1);
		}
		
		Client.setCurrentClient(new Client(p, user, passwd, null));
		System.out.println("Network Client started");
		
		String tr = p.getProperty("server.terminal");
		if (tr != null && tr.equals("on")) {
			Thread t = new Thread(new NetworkClient("http://localhost/", 10, 6767, false));
			t.setName("Server Terminal Thread");
			t.start();
		}
	}
}
