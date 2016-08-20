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
 * Copyright (C) 2006  Mike Murray
 */

package org.almostrealism.flow.tests;

import org.almostrealism.flow.Job;
import org.almostrealism.flow.JobFactory;
import org.almostrealism.flow.Server;

public class UrlProfilingTask implements JobFactory {
	public static interface Producer {
		public String nextURL();
		public int nextSize();
		public void set(String key, String value);
		public String encode();
	}
	
	private long id;
	private double pri = 1.0;
	private Producer producer;
	
	public long getTaskId() { return this.id; }

	public Job nextJob() {
		if (this.producer == null) return null;
		
		return new UrlProfilingJob(this.id,
									this.producer.nextURL(),
									this.producer.nextSize());
	}

	public Job createJob(String data) { return Server.instantiateJobClass(data); }

	public void set(String key, String value) {
		if (key.equals("id")) {
			this.id = Long.parseLong(value);
		} else if (key.equals("producer")) {
			try {
				this.producer = (Producer) Class.forName(value).newInstance();
			} catch (InstantiationException ie) {
				throw new RuntimeException("Could not instantiate producer (" +
						ie.getMessage() + ")");
			} catch (IllegalAccessException ae) {
				throw new RuntimeException("Could not instantiate producer (" +
						ae.getMessage() + ")");
			} catch (ClassNotFoundException cnf) {
				throw new RuntimeException("Could not find producer class (" +
						cnf.getMessage() + ")");
			}
		} else if (this.producer != null) {
			this.producer.set(key, value);
		} else if (this.producer == null) {
			this.producer = new DefaultProducer();
			this.producer.set(key, value);
		}
	}

	public String encode() {
		StringBuffer b = new StringBuffer();
		
		b.append(this.getClass().getName());
		b.append(":id=");
		b.append(this.id);
		
		if (this.producer != null) {
			b.append(":producer=");
			b.append(this.producer.getClass().getName());
			b.append(this.producer.encode());
		}
		
		return b.toString();
	}
	
	public String getName() { return "UrlProfilingTask (" + this.id + ")"; }
	
	public double getCompleteness() { return 0; }
	public boolean isComplete() { return false; }
	public void setPriority(double p) { this.pri = p; }
	public double getPriority() { return this.pri; }
}
