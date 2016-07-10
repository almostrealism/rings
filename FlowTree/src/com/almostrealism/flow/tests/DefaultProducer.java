/*
 * Copyright (C) 2006  Mike Murray
 */

package com.almostrealism.flow.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.almostrealism.flow.tests.UrlProfilingTask.Producer;
import com.almostrealism.raytracer.Settings;

public class DefaultProducer implements Producer {
	private String dir, uri, sufix;
	private List files;
	private int size = 10;
	
	public void init() {
		InputStream is = null;
		
		try {
			is = new URL(this.dir).openStream();
		} catch (IOException ioe) {
			throw new RuntimeException("DefaultProducer: Error initializing -- " +
								ioe.getMessage());
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
			throw new RuntimeException("DefaultProducer: Error initializing -- " +
					ioe.getMessage());
		}
		
		System.out.println("DefaultProducer: Initialized with " + this.files.size() + " URLs.");
	}
	
	public String nextURL() {
		StringBuffer b = new StringBuffer();
		
		if (this.uri != null) b.append(this.uri);
		b.append(this.files.get(Settings.random.nextInt(this.files.size())));
		if (this.sufix != null) b.append(this.sufix);
		
		return b.toString();
	}

	public int nextSize() { return this.size; }

	public void set(String key, String value) {
		if (key.equals("dir")) {
			this.dir = value;
			this.init();
		} else if (key.equals("uri")) {
			this.uri = value;
			if (this.dir == null) this.set("dir", uri + "files.txt");
		} else if (key.equals("size")) {
			this.size = Integer.parseInt(value);
		}
	}

	public String encode() {
		StringBuffer b = new StringBuffer();
		
		if (this.dir != null) {
			b.append(":dir=");
			b.append(this.dir);
		}
		
		if (this.uri != null) {
			b.append(":uri=");
			b.append(this.uri);
		}
		
		b.append(":size=");
		b.append(this.size);
		
		return b.toString();
	}
}
