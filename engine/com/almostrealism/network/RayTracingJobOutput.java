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

import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.almostrealism.color.RGB;
import org.almostrealism.io.JobOutput;

/**
 * A RayTracingJobOutput object stores 
 * 
 * @author Mike Murray
 */
public class RayTracingJobOutput extends JobOutput implements Externalizable {
	private List data;
	
	private long taskId = -1;
	private int x, y, dx, dy;
	
	/**
	 * Constructs a new RayTracingJobOutput object.
	 */
	public RayTracingJobOutput() { this.data = new ArrayList(); }
	
	/**
	 * Constructs a new RayTracingJobOutput object using the specified username and password.
	 * 
	 * @param user  Username to use.
	 * @param passwd  Password to use.
	 * @param data  A string of the form "jobId:x:y:dx:dy".
	 */
	public RayTracingJobOutput(String user, String passwd, String data) {
		super(user, passwd, data);
		
		this.data = new ArrayList();
	}
	
	/**
	 * @return Returns the dx.
	 */
	public int getDx() { return dx; }
	
	/**
	 * @return Returns the dy.
	 */
	public int getDy() { return dy; }
	
	/**
	 * @return Returns the taskId.
	 */
	public long getTaskId() { return taskId; }
	
	/**
	 * @return Returns the x.
	 */
	public int getX() { return x; }
	
	/**
	 * @return Returns the y.
	 */
	public int getY() { return y; }
	
	public void setOutput(String data) {
		int index = data.indexOf(":");
		
		String value = null;
		
		j: for (int j = 0; ; j++) {
			if (data.charAt(index + 1) == '/') index = data.indexOf(":", index + 1);
			
			value = null;
			
			if (index <= 0)
				value = data;
			else
				value = data.substring(0, index);
			
			if (value.length() <= 0) break j;
			
			if (j == 0) {
				this.taskId = Long.parseLong(value);
			} else if (j == 1) {
				this.x = Integer.parseInt(value);
			} else if (j == 2) {
				this.y = Integer.parseInt(value);
			} else if (j == 3) {
				this.dx = Integer.parseInt(value);
			} else if (j == 4) {
				this.dy = Integer.parseInt(value);
			} else {
				this.data.add(RGB.parseRGB(value));
			}
			
			if (value == data) break j;
			
			data = data.substring(index + 1);
			index = data.indexOf(":");
		}
	}
	
	public String getOutput() {
		StringBuffer b = new StringBuffer();
		b.append(super.getOutput());
		
		Iterator itr = this.data.iterator();
		while(itr.hasNext()) b.append(":" + itr.next().toString());
		
		return b.toString();
	}
	
	/**
	 * Adds the specified RGB object to the list of color data stored by this
	 * RayTracingJobOutput object.
	 * 
	 * @param rgb  RGB object to add.
	 * @return  True if the object was added, false otherwise.
	 */
	public boolean addRGB(RGB rgb) { return this.data.add(rgb); }
	
	/**
	 * @return  An Iterator object for the RGB objects stored by this RayTracingJobOutput object.
	 */
	public Iterator iterator() { return this.data.iterator(); }
	
	/** 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(super.getUser());
		out.writeUTF(super.getPassword());
		out.writeLong(super.getTime());
		out.writeUTF(super.getOutput());
		
		Iterator itr = this.data.iterator();
		
		while (itr.hasNext()) {
			RGB rgb = (RGB) itr.next();
			out.writeObject(rgb);
		}
		
		out.writeObject(null);
	}

	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.setUser(in.readUTF());
		super.setPassword(in.readUTF());
		super.setTime(in.readLong());
		super.setOutput(in.readUTF());
		
		String data = super.getOutput();
		
		w: while (true) {
			try {
				RGB rgb = (RGB) in.readObject();
				
				if (rgb == null)
					break w;
				else
					this.data.add(rgb);
			} catch (EOFException eof) { break w; }
		}
		
		String d = data.toString();
		
		int index = data.indexOf(":");
		
		j: for (int j = 0; ; j++) {
			if (data.charAt(index + 1) == '/') index = data.indexOf(":", index + 1);
			
			String value = null;
			
			if (index <= 0)
				value = data;
			else
				value = data.substring(0, index);
			
			if (value.length() <= 0) break j;
			
			if (j == 0) {
				this.taskId = Long.parseLong(value);
			} else if (j == 1) {
				this.x = Integer.parseInt(value);
			} else if (j == 2) {
				this.y = Integer.parseInt(value);
			} else if (j == 3) {
				this.dx = Integer.parseInt(value);
			} else if (j == 4) {
				this.dy = Integer.parseInt(value);
			} else {
				break j;
			}
			
			data = data.substring(index + 1);
			index = data.indexOf(":");
		}
		
//		int t = this.x * this.y;
//		
//		for (int i = 0; i < t; i++) {
//			this.data.add(new RGB(in.readDouble(), in.readDouble(), in.readDouble()));
//			this.data.add((RGB)in.readObject());
//		}
	}
}
