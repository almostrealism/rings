/*
 * Copyright 2020 Michael Murray
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

import io.flowtree.job.JobFactory;
import org.almostrealism.color.RGB;
import org.almostrealism.io.JobOutput;

/**
 * A {@link RayTracingJobOutput} stores
 * 
 * @author Mike Murray
 */
public class RayTracingJobOutput extends JobOutput implements Externalizable {
	private List data;

	private int x, y, dx, dy;
	
	/**
	 * Constructs a new RayTracingJobOutput object.
	 */
	public RayTracingJobOutput() { this.data = new ArrayList(); }
	
	/**
	 * Constructs a new {@link RayTracingJobOutput} using the specified username and password.
	 * 
	 * @param user  Username to use.
	 * @param passwd  Password to use.
	 * @param data  A string of the form "jobId:x:y:dx:dy".
	 */
	// TODO  Now that the ID is part of the parent, there is no need for it to be embedded in the data
	public RayTracingJobOutput(String taskId, String user, String passwd, String data) {
		super(taskId, user, passwd, data);
		
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
	 * @return Returns the x.
	 */
	public int getX() { return x; }
	
	/**
	 * @return Returns the y.
	 */
	public int getY() { return y; }

	@Override
	public void setOutput(String data) {
		super.setOutput(data);

		String originalData = data;

		int index = data.indexOf(JobFactory.ENTRY_SEPARATOR);
		
		String value = null;
		
		j: for (int j = 0; ; j++) {
			if (data.charAt(index + 1) == '/') index = data.indexOf(JobFactory.ENTRY_SEPARATOR, index + JobFactory.ENTRY_SEPARATOR.length());
			
			value = null;
			
			if (index <= 0)
				value = data;
			else
				value = data.substring(0, index);
			
			if (value.length() <= 0) break j;
			
			if (j == 0) {
				setTaskId(value);
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
			
			data = data.substring(index + JobFactory.ENTRY_SEPARATOR.length());
			index = data.indexOf(JobFactory.ENTRY_SEPARATOR);
		}

		if (dx == 0 || dy == 0) {
			throw new IllegalArgumentException("Invalid dx/dy for " + originalData);
		}
	}

	@Override
	public String getOutput() {
		StringBuilder b = new StringBuilder();
		b.append(super.getOutput());
		for (Object datum : this.data)
			b.append(JobFactory.ENTRY_SEPARATOR).append(datum.toString());
		
		return b.toString();
	}
	
	/**
	 * Adds the specified RGB object to the list of color data stored by this
	 * {@link RayTracingJobOutput}.
	 * 
	 * @param rgb  RGB object to add.
	 * @return  True if the object was added, false otherwise.
	 */
	public boolean addRGB(RGB rgb) { return this.data.add(rgb); }
	
	/**
	 * @return  An Iterator object for the RGB objects stored by this {@link RayTracingJobOutput}.
	 */
	public Iterator iterator() { return this.data.iterator(); }

	/**
	 * @return  The number of {@link RGB}s stored by this {@link RayTracingJobOutput}.
	 */
	public int size() { return this.data.size(); }
	
	/** 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
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
	@Override
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
		
		String originalData = data.toString();
		
		int index = data.indexOf(JobFactory.ENTRY_SEPARATOR);
		
		j: for (int j = 0; ; j++) {
			if (data.charAt(index + 1) == '/') index = data.indexOf(JobFactory.ENTRY_SEPARATOR, index + JobFactory.ENTRY_SEPARATOR.length());
			
			String value = null;
			
			if (index <= 0)
				value = data;
			else
				value = data.substring(0, index);
			
			if (value.length() <= 0) break j;
			
			if (j == 0) {
				this.setTaskId(value);
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
			
			data = data.substring(index + JobFactory.ENTRY_SEPARATOR.length());
			index = data.indexOf(JobFactory.ENTRY_SEPARATOR);
		}

		if (dx == 0 || dy == 0) {
			throw new IllegalArgumentException("Invalid dx/dy for " + originalData);
		}
		
//		int t = this.x * this.y;
//		
//		for (int i = 0; i < t; i++) {
//			this.data.add(new RGB(in.readDouble(), in.readDouble(), in.readDouble()));
//			this.data.add((RGB)in.readObject());
//		}
	}
}
