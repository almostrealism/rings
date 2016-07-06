/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.physics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import com.almostrealism.flow.Message;
import com.almostrealism.flow.db.Client;
import com.almostrealism.io.FileEncoder;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.network.JobProducer;


/**
 * @author Mike Murray
 */
public class RenderingClient implements Runnable {
	public static final int SLEEP = 1800;
	
	private String httpdir, httpwww, simdir, simfile;
	private int w, h, ssw, ssh, jobSize;
	private String host;
	private int port;
	
	private double lastTime = -1.0;
	
	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("Please supply path to config file.");
			System.exit(1);
		}
		
		Properties p = new Properties();
		
		try {
			p.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException fnf) {
			System.out.println("Config file not found: " + args[0]);
			System.exit(2);
		} catch (IOException ioe) {
			System.out.println("IO error loading config file: " + args[0]);
			System.exit(3);
		}
		
		String msgv = p.getProperty("network.msg.verbose", "false");
		Message.verbose = Boolean.parseBoolean(msgv);
		
		String httpdir = p.getProperty("http.dir", "./");
		String httpwww = p.getProperty("http.www", "http://localhost/");
		
		String simdir = p.getProperty("simulation.dir", "./");
		String simfile = p.getProperty("simulation.file", "latest.state");
		
		int w = Integer.parseInt(p.getProperty("render.w", "0"));
		int h = Integer.parseInt(p.getProperty("render.h", "0"));
		
		int ssw = Integer.parseInt(p.getProperty("render.ssw", "1"));
		int ssh = Integer.parseInt(p.getProperty("render.ssh", "1"));
		
		int jobSize = Integer.parseInt(p.getProperty("render.jobsize", "4"));
		
		if (Client.getCurrentClient() == null) {
			System.out.println("Starting network client...");
			
			String user = p.getProperty("render.host.user", "");
			String passwd = p.getProperty("render.host.passwd", "");
			
			try {
				Client.setCurrentClient(new Client(p, user, passwd, null));
			} catch (IOException ioe) {
				System.out.println("IO error starting network client: " + ioe.getMessage());
			}
		}
		
		Thread t = new Thread(new RenderingClient(httpdir, httpwww, simdir, simfile,
												w, h, ssw, ssh, jobSize));
		t.start();
	}
	
	public RenderingClient(String httpdir, String httpwww, String simdir, String simfile,
							int w, int h, int ssw, int ssh, int jobSize) {
		this.httpdir = httpdir;
		this.httpwww = httpwww;
		this.simdir = simdir;
		this.simfile = simfile;
		
		this.w = w;
		this.h = h;
		this.ssw = ssw;
		this.ssh = ssh;
		this.jobSize = jobSize;
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep((RenderingClient.SLEEP / 2) * 1000);
				
				Properties p = new Properties();
				p.load(new FileInputStream(this.simdir + this.simfile));
				
				Simulation s = new Simulation(this.w, this.h, new RigidBody[0], 1.0, 0);
				Iterator b = s.loadProperties(p).iterator();
				
				double time = s.getTime();
				
				if (time > this.lastTime) {
					while (b.hasNext()) {
						Surface sr = (Surface)b.next();
						s.addSurface(sr);
					}
					
					final String sceneName = "scene-" + time + ".xml";
					final String sceneFile = this.httpdir + sceneName;
					FileEncoder.encodeSceneFile(s.getScene(), new File(sceneFile), FileEncoder.XMLEncoding);
					System.out.println("Wrote scene file: " + sceneFile);
					
					Thread t = new Thread(new Runnable() {
						public void run() {
							JobProducer.main(new String[] {
											RenderingClient.this.httpwww + sceneName,
											RenderingClient.this.w + "x" + RenderingClient.this.h,
											RenderingClient.this.ssw + "x" + RenderingClient.this.ssh,
											String.valueOf(RenderingClient.this.jobSize)});
						}
					});
					
					t.start();
					
					this.lastTime = time;
				}
				
				Thread.sleep((RenderingClient.SLEEP / 2) * 1000);
			} catch (FileNotFoundException fnf) {
				System.out.println("File not found: " + this.simdir + this.simfile);
			} catch (IOException ioe) {
				System.out.println("IO error loading file: " + this.simdir + this.simfile);
			} catch (InterruptedException ie) { }
		}
	}
}
