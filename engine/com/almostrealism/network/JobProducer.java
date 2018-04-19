/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.network;

import java.io.IOException;

import io.flowtree.cli.FlowTreeCliServer;
import io.flowtree.node.Client;

// TODO  Add constructor that accepts a RayTracingEngine.RenderingProperties object.

/**
 * A {@link JobProducer} is used to produce and send jobs
 * to a network node.
 * 
 * @author  Michael Murray
 */
public class JobProducer {
  public static final String helpText = "Usage: JobProducer sceneURI WxH SSWxSSH jobsize hostname port\n" +
  											"\t sceneURI = The URI to the XML scene file to be rendered\n" +
											"\t WxH = The dimensions of the image to be produced\n" +
											"\t SSWxSSH = The supersample dimensions for each pixel\n" +
											"\t jobsize = The number of pixels to render per job\n" +
											"\t hostname = The host name of the network node to use\n" +
											"\t port = The port to connect to on the host";
  
  private String sceneURI;
  private int w, h, ssw, ssh, dx, dy;
  private int totalJobs;
  private int jobSize;
  private int i;
  private double pw = -1.0, ph = -1.0;
  private double fl = -1.0;
  private double clx, cly, clz;
  private double cdx, cdy, cdz;
  private double pri;
  
  private long jobId;
  
  private String host;
  private int port;
  private FlowTreeCliServer nc;

  	/**
  	 * Constructs a JobProducer object and uses it to produce and send jobs
  	 * described by the command line arguments.
  	 * 
  	 * @param args  See field JobProducer.helpText.
  	 */
	public static void main(String args[]) {
		JobProducer p = new JobProducer(args);
		System.out.print("Sending task: ");
		p.sendTask();
		System.out.println("Done");
	}
	
	public JobProducer(String args[]) {
		if (args.length < 8) {
			System.out.println("Error: Too few arguments");
			System.out.println(JobProducer.helpText);
			System.exit(1);
		}
		
		int w = Integer.parseInt(args[1].substring(0, args[1].indexOf("x")));
		int h = Integer.parseInt(args[1].substring(args[1].indexOf("x") + 1));
		
		int ssw = Integer.parseInt(args[2].substring(0, args[2].indexOf("x")));
		int ssh = Integer.parseInt(args[2].substring(args[2].indexOf("x") + 1));
		
		double pw = Double.parseDouble(args[3].substring(0, args[3].indexOf("x")));
		double ph = Double.parseDouble(args[3].substring(args[3].indexOf("x") + 1));
		
		double fl = Double.parseDouble(args[4]);
		
		int index;
		String s = args[5];
		
		index = s.indexOf(",");
		double clx = Double.parseDouble(s.substring(0, index));
		
		index = s.indexOf(",");
		s = s.substring(index + 1);
		index = s.indexOf(",");
		double cly = Double.parseDouble(s.substring(0, index));
		
		index = s.indexOf(",");
		s = s.substring(index + 1);
		index = s.indexOf(",");
		double clz = Double.parseDouble(s);
		
		index = 0;
		s = args[6];
		
		index = s.indexOf(",");
		double cdx = Double.parseDouble(s.substring(0, index));
		
		index = s.indexOf(",");
		s = s.substring(index + 1);
		index = s.indexOf(",");
		double cdy = Double.parseDouble(s.substring(0, index));
		
		index = s.indexOf(",");
		s = s.substring(index + 1);
		index = s.indexOf(",");
		double cdz = Double.parseDouble(s);
		
		int size = Integer.parseInt(args[7]);
		
		String host = null;
		int port = 7766;
		
		if (args.length > 10) host = args[10];
		if (args.length > 11) port = Integer.parseInt(args[11]);
		
		System.out.print("Initializing job producer: ");
		long id = -1;
		if (args.length > 8)
			id = Long.parseLong(args[8]);
		else
			id = System.currentTimeMillis();
		
		
		double pri = 1.0;
		if (args.length > 9) pri = Double.parseDouble(args[9]);
		
		this.init(args[0], w, h, ssw, ssh, size, id, pw, ph, fl,
										clx, cly, clz, cdx, cdy, cdz, pri);
		
		if (host != null) this.setServer(host, port);
		
		System.out.println("Used job ID " + id);
	}
	
	/**
	 * Constructs a new JobProducer object.
	 * 
	 * @param sceneURI  URI of XML scene file to render.
	 * @param w  Width of image.
	 * @param h  Height of image.
	 * @param ssw  Supersample width.
	 * @param ssh  Supersample height.
	 * @param jobSize  Number of pixels to render per job.
	 * @param jobId  Unique id to use for jobs.
	 */
	public void init(String sceneURI, int w, int h, int ssw, int ssh, int jobSize, long jobId,
						double pw, double ph, double fl, double clx, double cly, double clz,
						double cdx, double cdy, double cdz, double pri) {
		this.sceneURI = sceneURI;
		
		this.w = w;
		this.h = h;
		this.ssw = ssw;
		this.ssh = ssh;
		this.pw = pw;
		this.ph = ph;
		this.fl = fl;
		this.clx = clx;
		this.cly = cly;
		this.clz = clz;
		this.cdx = cdx;
		this.cdy = cdy;
		this.cdz = cdz;
		
		this.pri = pri;
		
		this.jobId = jobId;
		
		this.jobSize = jobSize;
		int l = (int)Math.ceil(Math.sqrt(jobSize));
		
		this.dx = l;
		this.dy = l;
		
		this.totalJobs = ((w * h) / (this.dx * this.dy));
	}
	
	public String getHost() {
		if (this.host == null)
			return Client.getCurrentClient().getServer().getPeers()[0];
		else
			return this.host;
	}
	
	public String sendTask() {
		RayTracingJobFactory f = new RayTracingJobFactory(this.sceneURI, this.w, this.h,
														this.ssw, this.ssh,
														this.jobSize, this.jobId);
		f.setProjectionWidth(this.pw);
		f.setProjectionHeight(this.ph);
		f.setFocalLength(this.fl);
		f.setCameraLocation(this.clx, this.cly, this.clz);
		f.setCameraDirection(this.cdx, this.cdy, this.cdz);
		
		f.setPriority(this.pri);
		
		Client.getCurrentClient().getServer().sendTask(f.encode(), 0);
		
		return Client.getCurrentClient().getServer().getPeers()[0];
	}
	
	/**
	 * Sends some jobs to the server.
	 * 
	 * @param total  Number of jobs to send.
	 * @throws IOException  If an IOException is thrown by NetworkClient.sendJob
	 */
	public void sendJobs(int total) throws IOException {
		if (this.nc == null && Client.getCurrentClient() == null) {
				System.out.println("JobProducer: Network client not initialized.");
				return;
		}
		
		for (; this.i < total; this.i++) {
			int l = this.i * this.dx;
			int x = (l / this.w) * this.dx;
			int y = l % this.w;
			
			RayTracingJob j = new RayTracingJob(this.sceneURI, x, y, this.dx, this.dy,
													this.w, this.h, this.ssw, this.ssh, this.jobId);
			
			Client.getCurrentClient().getServer().sendTask(j.encode(), 0);
		}
	}
	
	/**
	 * Sends all jobs to the server.
	 * 
	 * @throws IOException  If an IOException is thrown by NetworkClient.sendJob
	 */
	public void sendAllJobs() throws IOException {
		this.sendJobs(this.totalJobs);
	}
	
	/**
	 * Sets the hostname and port of the server (network node) to send jobs to.
	 * 
	 * @param host  Hostname of server.
	 * @param port  Port of server.
	 */
	public void setServer(String host, int port) {
		this.i = 0;
		
		this.host = host;
		this.port = port;
        
        System.out.print("\tStarting network client: ");

        try {
        	this.nc = new FlowTreeCliServer(this.host, 10, this.port, false);
        } catch (Exception e) {
			e.printStackTrace();
        }
	}
}
