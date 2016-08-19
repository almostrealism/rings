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

package com.almostrealism.imaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.almostrealism.flow.Job;
import com.almostrealism.flow.JobFactory;
import com.almostrealism.flow.Server;

public class ArtifactDetectionTask implements JobFactory {
	private static int width = 9700;
	
	private long id;
	private String dir;
	private String uri;
	private int dx = 100;
	private boolean bot;
	
	private int lastX = 0;
	private String currentFile;
	private int currentIndex;
	private List files;
	
	private double pri = 1.0;
	private boolean complete;
	
	public void init() {
		InputStream is = null;
		
		try {
			is = new URL(this.dir).openStream();
		} catch (MalformedURLException urle) {
			System.out.println("ArtifactDetectionTask: Error initializing -- " +
								urle.getMessage());
			this.complete = true;
			return;
		} catch (IOException ioe) {
			System.out.println("ArtifactDetectionTask: Error initializing -- " +
								ioe.getMessage());
			this.complete = true;
			return;
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		this.files = new ArrayList();
		
		String line;
		
		try {
			line = in.readLine();
		
			while (line != null) {
				this.files.add(line);
				line = in.readLine();
			}
			
			in.close();
		} catch (IOException ioe) {
			System.out.println("ArtifactDetectionTool: " + ioe);
		}
		
		
		if (this.files.size() <= 0) {
			this.complete = true;
			return;
		}
		
		this.currentIndex = 0;
		this.currentFile = (String) this.files.get(0);
		this.complete = false;
	}
	
	public long getTaskId() { return this.id; }
	
	public Job nextJob() {
		if (this.currentFile == null) return null;
		
		ArtifactDetectionJob j = new ArtifactDetectionJob();
		j.set("id", String.valueOf(this.id));
		j.set("x", String.valueOf(this.lastX));
		j.set("dx", String.valueOf(this.dx));
		j.set("uri", this.uri + currentFile);
		j.set("b", String.valueOf(this.bot));
		
		this.lastX = this.lastX + this.dx;
		
		if (this.lastX >= ArtifactDetectionTask.width) {
			this.lastX = 0;
			
			if (this.currentIndex >= this.files.size() - 1) {
				this.complete = true;
				return j;
			}
			
			this.currentIndex++;
			this.currentFile = (String) this.files.get(this.currentIndex);
		}
		
		return j;
	}
	
	public Job createJob(String data) { return Server.instantiateJobClass(data); }
	
	public void set(String key, String value) {
		if (key.equals("id")) {
			this.id = Long.parseLong(value);
		} else if (key.equals("dir")) {
			this.dir = value;
			this.init();
		} else if (key.equals("uri")) {
			this.uri = value;
			if (this.dir == null) this.set("dir", uri + "files.txt");
		} else if (key.equals("dx")) {
			this.dx = Integer.parseInt(value);
		} else if (key.equals("b")) {
			this.bot = Boolean.parseBoolean(value);
		}
	}
	
	public String encode() {
		StringBuffer b = new StringBuffer();
		
		b.append(this.getClass().getName());
		b.append(":id=");
		b.append(this.id);
		b.append(":dir=");
		b.append(this.dir);
		b.append(":uri=");
		b.append(this.uri);
		b.append(":dx=");
		b.append(this.dx);
		b.append(":b=");
		b.append(this.bot);
		
		return b.toString();
	}
	
	public String toString() { return "ArtifactDetectionTask (" + this.id + ")"; }
	public String getName() { return "ArtifactDetectionTask"; }
	public double getCompleteness() { return 0.0; }
	public boolean isComplete() { return this.complete; }
	public void setPriority(double p) { this.pri = p; }
	public double getPriority() { return this.pri; }
	
	public static void main(String args[]) throws IOException {
		File f = new File(args[0]);
		String files[] = f.list();
		
		File listf = new File(f, "files.txt");
		
		try (BufferedWriter out = new BufferedWriter(new FileWriter(listf))) {
			for (int i = 0; i < files.length; i++) {
				out.write(files[i] + "\n");
			}
			
			out.flush();
		}
		
		System.out.println("Wrote " + listf.getName());
	}
}
