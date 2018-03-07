/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.raytracer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import io.flowtree.NetworkClient;
import io.flowtree.msg.Message;
import io.flowtree.node.Client;

import io.flowtree.fs.OutputServer;

/**
 * The RayTracingServer class provides a main method that starts a DB Server
 * and a Network Client using a properties file specified by the command line
 * argument.
 * 
 * @author  Michael Murray
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
			Thread t = new Thread(new NetworkClient("http://localhost/", 10,6767, false));
			t.setName("Server Terminal Thread");
			t.start();
		}
	}
}
