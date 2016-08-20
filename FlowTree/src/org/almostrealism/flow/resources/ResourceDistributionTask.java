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
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package org.almostrealism.flow.resources;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.almostrealism.flow.Job;
import org.almostrealism.flow.JobFactory;
import org.almostrealism.flow.Message;
import org.almostrealism.flow.NodeProxy;
import org.almostrealism.flow.Resource;
import org.almostrealism.flow.Server;
import org.almostrealism.flow.Server.ResourceProvider;
import org.almostrealism.flow.db.Client;
import org.almostrealism.flow.db.DatabaseConnection;
import org.almostrealism.flow.db.JobOutput;
import org.almostrealism.flow.db.OutputHandler;
import org.almostrealism.flow.db.OutputServer;
import org.almostrealism.flow.db.Query;
import org.almostrealism.flow.db.QueryHandler;
import org.almostrealism.flow.db.Query.ResultHandler;

/**
 * A ResourceDistributionTask object maintains a collection of Job instances that are cycled
 * into the queue of a running Rings client as necessary for moving chunks of data (from
 * DistributedResource objects that have been loaded) to peer machines.
 * These Job instances (ResourceDistributionJob) are designed to select the least
 * recently/frequently accessed pieces of data and move them to peer computers.
 * ResourceDistributionTask also implements the OutputHandler interface. When a
 * ResourceDistributionJob instance is used to move stored data to a peer node this
 * OutputHandler instance on the remote machine is used to commit the data using what
 * ever storage method is configured for the Rings client and also to retrieve the
 * data when it is requested by a ResourceDistributionJob for transmitting to a remote
 * client. The storage method could be a relational database using DefaultOutputHandler,
 * a collection of data files in a local directory or any other way of storing information 
 * on the local machine.
 * 
 * @author Mike Murray
 */
public class ResourceDistributionTask implements JobFactory, OutputHandler, QueryHandler,
													NodeProxy.EventListener, Server.ResourceProvider {
	public static boolean verbose = false;
	public static boolean queryVerbose = false;
	public static long maxCache = 250 * 1000 * 1000;
	
	private static ResourceDistributionTask current;
	private static List resourceTypes = new ArrayList();
	
	public static interface InvalidateListener { public void fireInvalidate(); }
	private class Directory {
		String uri;
		ResourceProvider provider;
	}
	
	private static class Loader implements Runnable {
		DistributedResource res;
		InputStream in;
		
		public Loader(DistributedResource res, InputStream in) {
			this.res = res;
			this.in = in;
		}
		
		public void run() {
			if (DistributedResource.ioVerbose) {
				System.out.println("ResourceDistributionTask.Loader (" +
									this.res + "): Started");
			}
			
			try {
				this.res.loadFromStream(in);
			} catch (IOException e) {
				System.out.println("ResourceDistributionTask.Loader: " + e);
			}
			
			if (DistributedResource.ioVerbose) {
				System.out.println("ResourceDistributionTask.Loader (" +
									this.res + "): Ended");
			}
		}
	}
	
	private class CustomResultHandler implements Query.ResultHandler {
		private int fewest = Integer.MAX_VALUE;
		private long toa = -1;
		
		public void handleResult(String key, String value) {
			if (value == null || value.length() <= 0) value = "0";
			int dup = Integer.parseInt(value);
			
			if (dup < fewest) {
				toa = Long.parseLong(key);
				fewest = dup;
			}
			
			if (ResourceDistributionTask.queryVerbose)
				System.out.println("ResourceDistributionTask.CustomResultHandler: "
									+ key + " " + value);
		}
		
		public void handleResult(String key, byte value[]) {
			System.out.println("ResourceDistributionTask.CustomResultHandler: " +
								"Recieved bytes when string was expected.");
		}
		
		public long getToa() {
			if (this.toa <= 0) return this.toa;
			
			OutputServer.getCurrentServer().
				getDatabaseConnection().
				updateDuplication(this.toa, fewest + 1);
			
			return this.toa;
		}
	}
	
	protected static class ResourceDistributionJob implements Job {
		private long id;
		private int sleep;
		private boolean use;
		
		private String uri, data;
		private int index;
		
		private ResourceDistributionTask task;
		
		public ResourceDistributionJob() { }
		public ResourceDistributionJob(ResourceDistributionTask task) { this.task = task; }
		
		public void setSleep(int sleep) { this.sleep = sleep; }
		public boolean isInUse() { return this.use; }
		public void setInUse(boolean use) { this.use = use; }
		
		public void setData(String data) { this.data = data; }
		
		/**
		 * Requests a piece of data from the ResourceDistributionTask and converts
		 * it to a String. The String also includes valid information to reconstruct
		 * the ResourceDistributionJob within a remotely running Server instance.
		 * The remote Server instance will reference the set method of this class
		 * to reconstruct the state of the object. When the set method is called
		 * with the value String containing the piece of data, the data will be
		 * sent to the output server specified by the remote Server.
		 */
		public String encode() {
			if (this.task == null) return null;
			
			if (!this.task.loadJob(this)) {
				System.out.println("ResourceDistributionJob: No data available.");
				return null;
			}
			
			this.task.loadJob(this);
			
			StringBuffer b = new StringBuffer();
			
			b.append(this.getClass().getName());
			b.append(":uri=");
			b.append(this.uri);
			b.append(":i=");
			b.append(this.index);
			b.append(":RAW:");
			b.append(this.data);
			
			return b.toString();
		}
		
		public void set(String key, String value) {
			if (key.equals("uri")) {
				this.uri = value;
			} else if (key.equals("i")) {
				this.index = Integer.parseInt(value);
			} else if (key.equals("data")) {
				this.data = value;
			} else {
				OutputServer s = OutputServer.getCurrentServer();
				if (s == null) return;
				
				s.getDatabaseConnection().storeOutput(System.currentTimeMillis(),
														value.getBytes(),
														this.uri, this.index);
			}
		}
		
		public long getTaskId() { return this.id; }
		public String getTaskString() { return "ResourceDistributionTask (" + this.id + ")"; }
		
		/**
		 * Sleeps for a set amount of time and then marks this ResourceDistributionJob
		 * as ready to be reused.
		 */
		public void run() {
			if (this.task == null) return;
			
			try {
				Thread.sleep(this.sleep);
			} catch (InterruptedException ie) { }
			
			this.use = false;
		}
	}
	
	private long id;
	private Set jobs;
	private int sleep;
	
	private long cacheTot;
	
	private InvalidateListener inListen;
	
	private Hashtable items;
	private OutputServer server;
	
	public ResourceDistributionTask(int jobs, int sleep) {
		ResourceDistributionTask.current = this;
		
		this.jobs = new HashSet();
		this.items = new Hashtable();
		this.sleep = sleep;
		
		this.initDefaultResourceTypes();
		this.initFiles();
		this.initJobs(jobs);
	}
	
	protected void initDefaultResourceTypes() {
		ResourceDistributionTask.addResourceClass(
				new ConcatenatedResource.ConcatenatedResourceHeaderParser());
		System.out.println("ResourceDistributionTask: Added ConcatenatedResource type.");
	}
	
	protected void initFiles() {
		Directory fdir = new Directory();
		fdir.uri = "/files/";
		this.items.put(fdir.uri, fdir);
		
		this.server = OutputServer.getCurrentServer();
		
		System.out.println("ResourceDistributionTask: Loading file list from local DB.");
		
		DatabaseConnection db = this.server.getDatabaseConnection();
		
		Query q = new Query(db.getTable());
		q.setColumn(0, db.uriColumn);
		q.setColumn(1, null);
		q.setResultHandler(new ResultHandler() {
			public void handleResult(String key, String value) {
				if (!ResourceDistributionTask.this.items.containsKey(value)) {
					DistributedResource res = DistributedResource.createDistributedResource(value);
					ResourceDistributionTask.this.items.put(value, res);
					
					if (ResourceDistributionTask.verbose)
						System.out.println("ResourceDistributionTask: Loaded " + value +
											" from local DB.");
				}
			}
			
			public void handleResult(String key, byte value[]) {
					System.out.println("ResourceDistributionTask: Recieved bytes " +
										"when String was expected.");
			}
		});
		
		db.executeQuery(q);
	}
	
	protected void initJobs(int tot) {
		this.jobs.clear();
		
		for (int i = 0; i < tot; i++) {
			this.jobs.add(new ResourceDistributionJob(this));
		}
	}
	
	/**
	 * This is a direct put to the items hash table. Be carefull.
	 * 
	 * @param uri  URI to use as key.
	 * @param d  DistributedResource to store.
	 */
	protected void put(String uri, DistributedResource d) { this.items.put(uri, d); }
	
	protected boolean loadJob(ResourceDistributionJob j) {
		String table = this.server.getDatabaseConnection().getTable();
		Query q = new Query(table, DatabaseConnection.toaColumn,
							DatabaseConnection.dupColumn, null);
		CustomResultHandler handler = new CustomResultHandler();
		q.setResultHandler(handler);
		
		DatabaseConnection db = this.server.getDatabaseConnection();
		db.executeQuery(q);
		
		long toa = handler.getToa();
		if (toa <= 0) return false;
		
		return db.configureJob(j, toa);
	}
	
	public synchronized DistributedResource createResource(String uri) {
		if (this.items.containsKey(uri)) return null;
		
		DistributedResource res = DistributedResource.createDistributedResource(uri);
		this.items.put(uri, res);
		
		this.notifyPeers(uri, Message.DistributedResourceUri);
		
		return res;
	}
	
	public synchronized String createDirectory(String uri) {
		if (!uri.endsWith("/")) uri = uri + "/";
		if (this.items.containsKey(uri)) return null;
		
		Directory res = new Directory();
		res.uri = uri;
		
		if (ResourceDistributionTask.verbose)
			System.out.println("ResourceDistributionTask: Create dir " + uri);
		
		this.items.put(uri, res);
		return uri;
	}
	
	public synchronized boolean setResourceProvider(String dir, ResourceProvider p) {
		Object o = null;
		
		if (this.isDirectory(dir)) o = this.items.get(dir);
		
		if (o instanceof Directory == false) {
			System.out.println("ResourceDistributionTask.setResourceProvider: " + dir +
								" is not a directory.");
			return false;
		}
		
		Directory d = (Directory) o;
		d.provider = p;
		
		return true;
	}
	
	public synchronized boolean deleteResource(String uri) {
		if (!this.items.containsKey(uri)) return false;
		if (uri.equals("/")) return false;
		
		this.items.remove(uri);
		this.notifyPeers(uri, Message.DistributedResourceInvalidate);
		this.removeFromLocalDB(uri);
		
		if (ResourceDistributionTask.verbose)
			System.out.println("ResourceDistributionTask: Deleted " + uri);
		
		return true;
	}
	
	/**
	 * Deletes the specified directory and recursively deletes the contents
	 * from the distributed database.
	 * 
	 * @param uri  Path to directory.
	 * @return  True if the directory and contents was successfully deleted, false otherwise.
	 */
	public synchronized boolean deleteDirectory(String uri) {
		String s[] = this.getChildren(uri);
		
		boolean deleted = true;
		
		for (int i = 0; i < s.length; i++) {
			if (this.isDirectory(s[i])) {
				if (!this.deleteDirectory(s[i])) deleted = false;
			} else {
				if (!this.deleteResource(s[i])) deleted = false;
			}
		}
		
		return deleted;
	}
	
	public synchronized String getParent(String uri) {
		if (uri.equals("/")) return null;
		
		if (uri.endsWith("/"))
			uri = uri.substring(0, uri.length() - 1);
		
		return uri.substring(0, uri.lastIndexOf("/"));
	}
	
	public synchronized String[] getChildren(String uri) {
		List names = new ArrayList();
		Iterator itr = this.items.keySet().iterator();
		
		if (!uri.endsWith("/")) uri = uri + "/";
		
		while (itr.hasNext()) {
			String s = (String) itr.next();
			while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
			
			if (s.startsWith(uri) && !s.equals(uri) && !names.contains(s)) {
				String ss = s.substring(uri.length() + 1);
				int si = ss.indexOf("/");
				
				if (si < 0 || si >= ss.length() - 1) {
					names.add(s);
				} else {
					s = s.substring(0, uri.length() + 1 + si);
					if (!names.contains(s)) names.add(s);
				}
			}
		}
		
		if (ResourceDistributionTask.verbose)
			System.out.println("ResourceDistributionTask: Got " + names.size() + " children " + uri);
		
		return (String[]) names.toArray(new String[0]);
	}
	
	public synchronized boolean isDirectory(String uri) {
		if (ResourceDistributionTask.verbose)
			System.out.println("ResourceDistributionTask: Is dir? " + uri);
		
		if (uri.equals("/")) return true;
		if (!uri.endsWith("/")) uri = uri + "/";
		if (this.items.get(uri) instanceof Directory) return true;
		
		if (this.getChildren(uri).length > 0) {
			Directory d = new Directory();
			d.uri = uri;
			this.items.put(d.uri, d);
			return true;
		}
		
		return false;
	}
	
	/**
	 * This method calls getResource using the specified uri.
	 * 
	 * @see getResource(String)
	 */
	public Resource loadResource(String uri) { return this.getResource(uri); }
	
	/**
	 * This method calls getResource using the specified uri and exclude string.
	 * 
	 * @see getResource(String, String)
	 */
	public Resource loadResource(String uri, String exclude) {
		return this.getResource(uri, exclude);
	}
	
	public OutputStream getOutputStream(String uri) throws IOException {
		DistributedResource res = this.getResource(uri);
		if (res == null) res = this.createResource(uri);
		if (res == null) return null;
		
		PipedOutputStream out = new PipedOutputStream();
		PipedInputStream in = new PipedInputStream(out);
		
		Thread t = new Thread(new Loader(res, in));
		t.start();
		
		return out;
	}
	
	protected void checkFull() {
		//TODO Add inteligent clutter removal...
		
		if (this.cacheTot > this.maxCache) {
			Object o[] = this.items.entrySet().toArray();
			Map.Entry ent = (Map.Entry) o[(int) (Math.random() * o.length)];
			DistributedResource r = (DistributedResource) ent.getValue();
			r.clear();
		}
	}
	
	protected void addCache(long tot) { this.cacheTot += tot; }
	protected void subtractCache(long tot) { this.cacheTot -= tot; }
	
	protected void removeFromLocalDB(String uri) {
		OutputServer s = OutputServer.getCurrentServer();
		
		if (s == null) {
			System.out.println("ResourceDistributionTask: Unable to remove " +
								uri + " (No local DB)");
			return;
		}
		
		s.getDatabaseConnection().deleteUri(uri);
	}
	
	public synchronized DistributedResource getResource(String uri) {
		return this.getResource(uri, null);
	}
	
	public synchronized DistributedResource getResource(String uri, String exclude) {
		if (ResourceDistributionTask.verbose)
			System.out.println("ResourceDistributionTask: Get " + uri);
		
		if (!uri.startsWith("/")) uri = "/" + uri;
		
		Object res = this.items.get(uri);
		if (res instanceof Directory) {
			ResourceProvider p = ((Directory) res).provider;
			if (p != null) res = p.loadResource(uri, exclude);
		}
		
		DistributedResource dres = null;
		
		if (res instanceof DistributedResource)
			dres = (DistributedResource) res;
		else if (res instanceof Resource) {
			System.out.println("ResourceDistributionTask: Item cache contained " + res.getClass());
			dres = new DistributedResource((Resource) res);
		} else
			return null;
		
		dres.setExcludeHost(exclude);
		return dres;
	}
	
	public int notifyPeers() {
		Iterator itr = this.items.entrySet().iterator();
		
		int tot = 0;
		
		w: while (itr.hasNext()) {
			Map.Entry ent = (Map.Entry) itr.next();
			
			if (ent.getValue() instanceof DistributedResource == false)
				continue w;
			
			this.notifyPeers((String) ent.getKey(), Message.DistributedResourceUri);
			tot++;
		}
		
		return tot;
	}
	
	public void setInvalidateListener(InvalidateListener listener) { this.inListen = listener; }
	
	protected void fireInvalidate() {
		if (this.inListen != null)
			this.inListen.fireInvalidate();
	}
	
	protected int notifyPeers(String uri, int type) {
		this.fireInvalidate();
		
		NodeProxy p[] = Client.getCurrentClient().getServer().getNodeGroup().getServers();
		int tot = 0;
		
		for (int i = 0; i < p.length; i++) {
			if (this.notifyPeer(uri, type, p[i]))
				tot++;
		}
		
		if (Message.verbose)
			System.out.println("ResourceDistributionTask: Notified " + tot +
								" peers of " + uri);
		
		return tot;
	}
	
	protected boolean notifyPeer(String uri, int type, NodeProxy np) {
		try {
			if (Message.verbose)
				System.out.println("ResourceDistributionTask: Notifing " + np + " of " + uri);
			
			Message m = new Message(type, -3, np);
			m.setQueueBypass(true);
			m.setString(uri);
			m.send(-3);
			
			return true;
		} catch (IOException ioe) {
			System.out.println("ResourceDistributionTask: IO error notifing " +
								np + " of " + uri + " (" + ioe.getMessage() + ")");
			return false;
		}
	}
	
	public Job createJob(String data) { return Server.instantiateJobClass(data); }
	
	public String encode() { return null; }
	
	public void setPriority(double p) { }
	public double getPriority() { return 1.0; }
	public double getCompleteness() { return 0; }
	public boolean isComplete() { return false; }
	
	public String getName() { return "ResourceDistributionTask (" + this.id + ")"; }
	public long getTaskId() { return this.id; }
	
	public Job nextJob() {
		if (Client.getCurrentClient().getServer().getPeers().length <= 0)
			return null;
		if (OutputServer.getCurrentServer() == null)
			return null;
		
		Iterator itr = this.jobs.iterator();
		
		while (itr.hasNext()) {
			ResourceDistributionJob j = (ResourceDistributionJob) itr.next();
			
			if (!j.isInUse()) {
				j.setSleep(this.sleep);
				j.setInUse(true);
				return j;
			}
		}
		
		return null;
	}
	
	public void set(String key, String value) { }
	
	/**
	 * Method called by the OutputServer when data arrives to be persisted into
	 * the disk cache maintained by this ResourceDistributionTask. The ResourceDistributionTask
	 * does not actually perform the storage and this method is provided only so that
	 * the ResourceDistributionTask will be notified when the state of the cache changes
	 * (used for creating popularity ratings).
	 */
	public void storeOutput(long time, int uid, JobOutput output) {
	}

	public Hashtable executeQuery(Query q) {
		if (!DatabaseConnection.uriColumn.equals(q.getColumn(0))) return null;
		String uri = q.getValue(0);
		
		int index = Integer.parseInt(q.getValue(1));
		if (index < 0) return null;
		
		DistributedResource r = (DistributedResource) this.items.get(uri);
		byte b[] = r.getData(index, false);
		
		Hashtable h = null;
		
		if (b != null) {
			h = new Hashtable();
			h.put(Integer.valueOf(index), b);
		}
		
		return h;
	}
	
	/**
	 * Adds the specified ResourceHeaderParser to the list of parser to be
	 * checked when a new resource is loaded.
	 * 
	 * @param p ResourceHeaderParser
	 */
	public static void addResourceClass(ResourceHeaderParser p) {
		resourceTypes.add(p);
	}
	
	/**
	 * Checks the known ResourceHeaderParsers to see if any of them indicate
	 * that an alternative class should be used for the resource with the
	 * specified header.
	 * 
	 * @param b  Header for resource.
	 * @return  Class to use.
	 */
	public static Class getResourceClass(byte b[]) {
		if (b == null) return DistributedResource.class;
		
		Iterator itr = resourceTypes.iterator();
		
		while (itr.hasNext()) {
			ResourceHeaderParser p = (ResourceHeaderParser) itr.next();
			
			if (p.doesHeaderMatch(b)) return p.getResourceClass();
		}
		
		return DistributedResource.class;
	}
	
	public static ResourceDistributionTask getCurrentTask() {
		return ResourceDistributionTask.current;
	}
	
	public void connect(NodeProxy p) {
		synchronized (this.items) {
			Iterator itr = this.items.entrySet().iterator();
			
			w: while (itr.hasNext()) {
				Map.Entry ent = (Map.Entry) itr.next();
				Object o = ent.getValue();
				if (o instanceof DistributedResource == false) continue w;
				this.notifyPeer((String) ent.getKey(), Message.DistributedResourceUri, p);
			}
		}
	}
	
	public int disconnect(NodeProxy p) { return 0; }
	
	public synchronized boolean recievedMessage(Message m, int reciever) {
		if (!(m.getType() == Message.DistributedResourceUri ||
			m.getType() == Message.DistributedResourceInvalidate)) return false;
		
		String data = m.getData();
		int size = -1;
		String uri = data;
		int index = data.lastIndexOf(":");
		
		if (index > 0) {
			uri = data.substring(0, index);
			size = Integer.parseInt(data.substring(index + 1));
		}
		
		if (m.getType() == Message.DistributedResourceInvalidate) {
			Object o = this.items.remove(uri);
			
			if (o != null) {
				System.out.println("ResourceDistributionTask: " + o + " was invalidated.");
				this.notifyPeers(uri, Message.DistributedResourceInvalidate);
				this.removeFromLocalDB(uri);
			}
		} else if (this.items.containsKey(uri)) {
			if (DistributedResource.verbose)
				System.out.println("ResourceDistributionTask: " + uri + " exists.");
			
			DistributedResource cr = (DistributedResource) this.items.get(uri);
			
			if (size >= 0 && cr.getSize() != size)
				System.out.println("ResourceDistributionTask: Size disagreement for " + cr +
									" (" + cr.getSize() + " != " + size + ")");
		} else {
			DistributedResource r = null;
			
			if (size < 0)
				r = DistributedResource.createDistributedResource(uri);
			else
				r = DistributedResource.createDistributedResource(uri, size);
			
			this.items.put(uri, r);
			System.out.println("ResourceDistributionTask: Added " + r);
			this.notifyPeers(uri, Message.DistributedResourceUri);
		}
		
		return true;
	}
	
	public String toString() {
		return "ResourceDistributionTask (" + this.jobs.size() + ", " + this.sleep + ")";
	}
}