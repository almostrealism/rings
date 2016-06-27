/*
 * Copyright (C) 2006  Mike Murray
 */

package net.sf.j3d.network.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.j3d.network.Job;
import net.sf.j3d.network.db.Client;
import net.sf.j3d.network.db.JobOutput;
import net.sf.j3d.network.db.OutputHandler;

public class UrlProfilingJob implements Job {
	public static class Handler implements OutputHandler {
		private PrintWriter out;
		
		public Handler() throws IOException {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter("htdocs/url-profile" +
							System.currentTimeMillis() + ".txt")));
		}
		
		public void storeOutput(long time, int uid, JobOutput output) {
			String s[] = output.getOutput().split(":");
			
			StringBuffer b = new StringBuffer();
			b.append(time);
			b.append("\t");
			
			for (int i = 0; i < s.length; i++) {
				b.append(s[i]);
				b.append("\t");
			}
			
			String bs = b.toString();
			synchronized (this.out) { this.out.println(bs); this.out.flush(); }
		}
	}
	
	private long id;
	private String uri;
	private int size;
	
	public UrlProfilingJob() { }
	
	public UrlProfilingJob(long id, String uri, int size) {
		this.id = id;
		this.uri = uri;
		this.size = size;
	}
	
	public long getTaskId() { return this.id; }

	public String getTaskString() { return "ImageProfilingTask (" + this.id + ")"; }

	public String encode() {
		StringBuffer b = new StringBuffer();
		
		b.append(this.getClass().getName());
		b.append(":id=");
		b.append(this.id);
		b.append(":uri=");
		b.append(this.uri);
		b.append(":size=");
		b.append(this.size);
		
		return b.toString();
	}

	public void set(String key, String value) {
		if (key.equals("id")) {
			this.id = Long.parseLong(value);
		} else if (key.equals("uri")) {
			this.uri = value;
		} else if (key.equals("size")) {
			this.size = Integer.parseInt(value);
		}
	}

	public void run() throws RuntimeException {
		long start, end, tot = 0, bs = 0;
		
		for (int i = 0; i < this.size; i++) {
			start = System.currentTimeMillis();
			
			String d[] = this.uri.split("\\\\");
			StringBuffer b = new StringBuffer();
			for (int j = 0; j < d.length; j++) b.append(d[j]);
			uri = b.toString();
			
			try {
				InputStream in = new URL(this.uri).openStream();
				while (in.available() > 0) { in.read(); bs++; }
				in.close();
			} catch (MalformedURLException murl) {
				throw new RuntimeException("UrlProfilingJob -- " + murl.getMessage());
			} catch (IOException e) { }
			
			end = System.currentTimeMillis();
			tot += end - start;
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		
		if (this.size <= 0) return;
		
		long avgTime = tot / this.size;
		long avgBs = bs / this.size;
		
		StringBuffer b = new StringBuffer();
		b.append(this.uri.substring(this.uri.lastIndexOf("/")));
		b.append(":");
		b.append(this.size);
		b.append(":");
		b.append(avgTime);
		b.append(":");
		b.append(avgBs);
		b.append(":");
		
		Client.getCurrentClient().writeOutput(new JobOutput("", "", b.toString()));
	}
	
	public String toString() {
		return "UrlProfilingJob (" + this.uri + ") size = " + this.size;
	}
}
