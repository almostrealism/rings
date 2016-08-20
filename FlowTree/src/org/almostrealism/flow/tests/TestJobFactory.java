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

/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package org.almostrealism.flow.tests;

import org.almostrealism.flow.Job;
import org.almostrealism.flow.JobFactory;

/**
 * @author Mike Murray
 */
public class TestJobFactory implements JobFactory {
	private double pri;
	private int jobs;
	
	public static class TestJob implements Job {
		private long id = -1;
		private int i = -1;
		private int sleep = 5000;
		
		public TestJob() {}
		
		public TestJob(int i, int sleep, long id) {
			this.i = i;
			this.sleep = sleep;
			this.id = id;
		}
		
		public void run() {
			try {
				Thread.sleep(this.sleep);
				System.out.println("TestJob: Slept " + this.sleep + " (i = " + this.i + ")");
			} catch (InterruptedException ie) {
				System.out.println("TestJob: Sleep interrupted.");
			}
		}
		
		public String encode() {
			return this.getClass().getName() + ":i=" + this.i + ":s=" + this.sleep + ":id=" + this.id;
		}
		
		public void set(String key, String value) {
			if (key.equals("i")) {
				this.i = Integer.parseInt(value);
			} else if (key.equals("s")) {
				this.sleep = Integer.parseInt(value);
			} else if (key.equals("id")) {
				this.id = Long.parseLong(value);
			}
		}
		
		public boolean equals(Object o) {
			if (o instanceof TestJob == false) return false;
			
			TestJob t = (TestJob) o;
			return (t.getTaskId() == this.getTaskId() && t.i == this.i);
		}
		
		public int hashCode() { return (int) ((this.getTaskId() + this.i) % (Integer.MAX_VALUE - 1)); }
		
		public void setTaskId(long id) { this.id = id; }
		public long getTaskId() { return this.id; }
		
		public String toString() { return this.encode(); }
		public String getTaskString() { return "System Test (" + this.id + ")"; }
	}
	
	private int sleep = 5000;
	private long id = -1;
	
	public TestJobFactory() { }
	public TestJobFactory(int sleep) { this.sleep = sleep; }
	
	/**
	 * @see org.almostrealism.flow.JobFactory#nextJob()
	 */
	public Job nextJob() { return new TestJob(this.jobs++, this.sleep, this.id); }
	
	/**
	 * @see org.almostrealism.flow.JobFactory#createJob(java.lang.String)
	 */
	public Job createJob(String data) { return org.almostrealism.flow.Server.instantiateJobClass(data); }
	
	/**
	 * @return  A String encoding of this RayTracingJobFactory object.
	 */
	public String encode() {
		StringBuffer buf = new StringBuffer();
		
		buf.append(this.getClass().getName());
		buf.append(":s=");
		buf.append(this.sleep);
		buf.append(":id=");
		buf.append(this.id);
		
		return buf.toString();
	}
	
	/**
	 * Sets the sleep time for this TestJobFactory.
	 */
	public void set(String key, String value) {
		if (key.equals("id"))
			this.id = Long.parseLong(value);
		else
			this.sleep = Integer.parseInt(value);
	}
	
	public boolean isComplete() { return false; }
	
	public void setTaskId(long id) { this.id = id; }
	
	/**
	 * @see org.almostrealism.flow.JobFactory#getTaskId()
	 * @return  The task id (default -1).
	 */
	public long getTaskId() { return this.id; }
	
	/**
	 * @return  "System Test (<id>)".
	 */
	public String getName() { return "System Test (" + this.id + ")"; }
	
	/**
	 * @return  0.0
	 */
	public double getCompleteness() { return 0; }
	public void setPriority(double p) { this.pri = p; }
	public double getPriority() { return this.pri; }
}
