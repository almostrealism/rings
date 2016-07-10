/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.NoSuchPaddingException;
import javax.swing.JLabel;

import com.almostrealism.flow.db.Client;
import com.almostrealism.io.RSSFeed;
import com.almostrealism.raytracer.Settings;
import com.almostrealism.util.Graph;

/**
 * A NodeGroup object represents a group of nodes (Node objects).
 * The NodeGroup object is responsible for moderating communication
 * with each of its children.
 * 
 * @author Mike Murray
 */
public class NodeGroup extends Node implements Runnable, NodeProxy.EventListener,
														Node.ActivityListener {
	private double activityO = -0.2;
	
	private int maxDuplicateConnections = 2;
	
	private JobFactory defaultFactory;
	
	private List nodes, servers, tasks, connecting;
	private List cachedTasks;
	private List plisteners;
	private int jobsPerTask = 1, maxTasks = 1;
	
	private char passwd[];
	private String crypt;
	
	private Graph activityGraph, throughputGraph;
	private int tpFreq = 5, tpLast = 0;
	private double activitySum, totalActivitySum;
	private int activityDivisor, totalActivityDiv;
	
	private Thread thread, monitor;
	private boolean stop;
	private int isolationTime;
	private int monitorSleep = 30000;
	
	private int jobWasNull = 1;
	
	/**
	 * Constructs a new NodeGroup object using the specified JobFactory object
	 * and the properties defined in the specified Properties object.
	 * 
	 * @param p  Properties object to read properties from.
	 * @param f  Source of jobs for this NodeGroup object.
	 */
	public NodeGroup(Properties p, JobFactory f) {
		super(null, 0, 0, 0);
		super.setName("Node Group");
		
		this.defaultFactory = f;
		
		this.setSleep(Integer.parseInt(p.getProperty("group.thread.sleep", "10000")));
		
		String pass = p.getProperty("group.proxy.password");
		if (pass != null) this.passwd = pass.toCharArray();
		
		this.crypt = p.getProperty("group.proxy.crypt");
		
		int nodeCount = Integer.parseInt(p.getProperty("nodes.initial", "1"));
		int nodeMaxJobs = Integer.parseInt(p.getProperty("nodes.jobs.max", "1"));
		int nodeMaxPeers = Integer.parseInt(p.getProperty("nodes.peers.max", "2"));
		
		int serverCount = Integer.parseInt(p.getProperty("servers.total", "0"));
		
		this.connecting = new ArrayList();
		this.tasks = new ArrayList();
		this.nodes = new ArrayList(nodeCount);
		this.cachedTasks = new ArrayList();
		
		if (nodeCount > 0) System.out.println("NodeGroup: Constructing child nodes...");
		
		for (int i = 0; i < nodeCount; i++) {
			Node n = new Node(this, i, nodeMaxJobs, nodeMaxPeers);
			this.nodes.add(n);
			
			System.out.println("NodeGroup: Added node " + i + " (" + n + ")");
		}
		
		this.setParam(p);
		
		this.servers = new ArrayList(serverCount);
		
		if (serverCount > 0) System.out.println("NodeGroup: Opening server connections...");
		
		for (int i = 0; i < serverCount; i++) {
			String host = p.getProperty("servers." + i + ".host", "localhost");
			int port = Integer.parseInt(p.getProperty("servers." + i + ".port", "7777"));
			
			try {
				System.out.println("NodeGroup: Connecting to server " + i + " (" + host + ":" + port + ")...");
				
				Socket s = new Socket(host, port);
				// s.setKeepAlive(true);
				
				this.addServer(s);
			} catch (UnknownHostException uh) {
				System.out.println("NodeGroup: Server " + i + " is unknown host");
			} catch (IOException ioe) {
				System.out.println("NodeGroup: IO error while connecting to server " +
								i + " -- " + ioe.getMessage());
			} catch (SecurityException se) {
				System.out.println("NodeGroup: Security exception while connecting to server " + i +
								" (" + se.getMessage() + ")");
			}
		}
		
		super.rssfile = p.getProperty("group.rss.file");
		String rsslink = p.getProperty("group.rss.url");
		
		if (rssfile != null) {
			SimpleDateFormat df = new SimpleDateFormat("h:mm a 'on' EEEE, MMMM d");
			super.log = new RSSFeed("Network Node Group Log", "Started at " + df.format(new Date()));
			
			if (rsslink != null) super.log.setLink(rsslink);
		}
		
		this.plisteners = new ArrayList();
		this.activityGraph = new Graph(Integer.MAX_VALUE - 1);
		this.throughputGraph = new Graph();
		super.sleepGraph = new Graph(Integer.MAX_VALUE - 1);
		
		Client c = Client.getCurrentClient();
		ThreadGroup g = null;
		if (c != null) g = c.getServer().getThreadGroup();
		this.thread = new Thread(g, this);
		this.thread.setName("Node Group Thread");
		this.thread.setPriority(Server.MODERATE_PRIORITY);
		
		this.monitor = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(NodeGroup.this.monitorSleep);
					} catch (InterruptedException ie) { }
					
					double aar = NodeGroup.this.getAverageActivityRating();
					NodeGroup.this.activitySum += aar;
					NodeGroup.this.totalActivitySum += aar;
					NodeGroup.this.activityDivisor++;
					NodeGroup.this.totalActivityDiv++;
					
					int s = NodeGroup.this.getSleep();
					NodeGroup.this.sleepSum += s;
					NodeGroup.this.totalSleepSum += s;
					NodeGroup.this.sleepDiv++;
					NodeGroup.this.totalSleepDiv++;
				}
			}
		});
		
		this.monitor.setDaemon(true);
	}
	
	/**
	 * Starts the thread that manages the activity of this NodeGroup and the threads
	 * for the child nodes stored by this NodeGroup.
	 */
	public void start() {
		this.stop = false;
		this.thread.start();
		
		synchronized (this.nodes) {
			Iterator itr = this.nodes.iterator();
			while (itr.hasNext()) ((Node)itr.next()).start();
		}
	}
	
	public void startMonitor(int priority, int sleep) {
		this.monitorSleep = sleep;
		this.monitor.setPriority(priority);
		this.monitor.start();
	}
	
	/**
	 * Stops the thread that manages the activity of this NodeGroup and the threads
	 * for the child nodes stored by this NodeGroup.
	 */
	public void stop() {
		this.stop = true;
		
		synchronized (this.nodes) {
			Iterator itr = this.nodes.iterator();
			while (itr.hasNext()) ((Node)itr.next()).stop();
		}
	}
	
	/**
	 * @return  True if any child node of this NodeGroup is working, false otherwise.
	 */
	public boolean isWorking() {
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) if (((Node)itr.next()).isWorking()) return true;
		
		return false;
	}
	
	public void setParam(Properties p) {
		Iterator itr = p.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry e = (Map.Entry) itr.next();
			String k = (String) e.getKey();
			String v = (String) e.getValue();
			
			try {
				this.setParam(k, v);
			} catch (NumberFormatException nfe) {
				System.out.println("NodeGroup: Error parsing number " +
							k + " = " + v + " (" + nfe.getMessage() + ")");
			}
		}
	}
	
	public boolean setParam(String name, String value) {
		String msg = null;
		
		if (name.equals("nodes.acs")) {
			msg = "ActivitySleepC = " + value;
			this.setActivitySleepC(Double.parseDouble(value));
		} else if (name.equals("nodes.pasc")) {
			msg = "PeerActivitySleepC = " + value;
			this.setPeerActivitySleepC(Double.parseDouble(value));
		} else if (name.equals("nodes.parp")) {
			msg = "ParentalRelayP = " + value;
			this.setParentalRelayP(Double.parseDouble(value));
		} else if (name.equals("nodes.prc")) {
			msg = "PeerRelayC = " + value;
			this.setPeerRelayC(Double.parseDouble(value));
		} else if (name.equals("nodes.mjp")) {
			msg = "MinimumJobP = " + value;
			this.setMinimumJobP(Double.parseDouble(value));
		} else if (name.equals("nodes.mfj")) {
			msg = "MaxFailedJobs = " + value;
			this.setMaxFailedJobs(Integer.parseInt(value));
		} else if (name.equals("group.aco")) {
			msg = "ActivityOffset = " + value;
			this.activityO = Double.parseDouble(value);
		} else if (name.equals("group.msc")) {
			msg = "MaxSleepC = " + value;
			this.setMaxSleepC(Double.parseDouble(value));
		} else if (name.equals("network.msg.verbose")) {
			Message.verbose = Boolean.parseBoolean(value);
		} else if (name.equals("network.msg.dverbose")) {
			Message.dverbose = Boolean.parseBoolean(value);
		} else if (name.equals("network.msg.sverbose")) {
			Message.sverbose = Boolean.parseBoolean(value);
		} else if (name.equals("group.nverbose")) {
			this.verbose = Boolean.parseBoolean(value);
		} else if (name.equals("group.taskjobs")) {
			msg = "TasksPerJob = " + value;
			this.jobsPerTask = Integer.parseInt(value);
		} else if (name.equals("group.taskmax")) {
			msg = "MaxTasks = " + value;
			this.maxTasks = Integer.parseInt(value);
		} else if (name.equals("nodes.relay")) {
			msg = "RelayP = " + value;
			this.setRelayProbability(Double.parseDouble(value));
		} else if (name.equals("nodes.wp")) {
			msg = "WeightPeers = " + value;
			this.setWeightPeers(Boolean.parseBoolean(value));
		} else {
			return false;
		}
		
		if (msg != null) {
			System.out.println("NodeGroup: " + msg);
			if (this.activityGraph != null) this.activityGraph.addMessage(msg);
		}
		
		return true;
	}
	
	// TODO Add tasks object value to documentation.
	public Object getObject(String key) {
		if (key.equals("group.tasks")) {
			return this.taskList();
		} else if (key.startsWith("group.tasks.")) {
			int i = Integer.parseInt(key.substring(12));
			return this.tasks.get(i);
		} else if (key.startsWith("node.")) {
			int in = key.indexOf(".", 5);
			boolean r = false;
			
			if (in < 0) {
				in = key.length();
				r = true;
			}
			
			int i = Integer.parseInt(key.substring(5, in));
			Node n = (Node) this.nodes.get(i);
			
			if (r)
				return n;
			else
				return n.getObject(key.substring(in + 1));
		} else {
			return null;
		}
	}
	
	/**
	 * @return  The set of Node objects stored by this NodeGroup object.
	 */
	public Node[] getNodes() { return (Node[])this.nodes.toArray(new Node[0]); }
	
	/**
	 * @return  The default JobFactory object used by this NodeGroup object.
	 */
	public JobFactory getJobFactory() { return this.defaultFactory; }
	
	public String[] taskList() {
		List l = new ArrayList();
		
		Iterator itr = this.tasks.iterator();
		while (itr.hasNext()) l.add(itr.next().toString());
		
		itr = this.cachedTasks.iterator();
		while (itr.hasNext()) l.add(itr.next());
		
		return (String[]) l.toArray(new String[0]);
	}
	
	public void addCachedTask(String task) {
		if (!this.cachedTasks.contains(task)) this.cachedTasks.add(task);
	}
	
	/**
	 * @return  The Node object stored by this NodeGroup object with the lowest connectivity rating.
	 */
	public Node getLeastConnectedNode() {
		Node n = null;
		
		synchronized (this.nodes) {
			Iterator itr = this.nodes.iterator();
			
			while (itr.hasNext()) {
				Node next = (Node)itr.next();
				
				if (n == null || n.getConnectivityRating() > next.getConnectivityRating())
					n = next;
			}
		}
		
		return n;
	}
	
	/**
	 * @return  The Node object stored by this NodeGroup object with the lowest activity rating.
	 */
	public Node getLeastActiveNode() {
		List l = new ArrayList();
		double rating = -1.0;
		
		synchronized (this.nodes) {
			Iterator itr = this.nodes.iterator();
			
			while (itr.hasNext()) {
				Node next = (Node)itr.next();
				double a = next.getActivityRating();
				
				if (rating == -1.0 || rating > a) {
					l.clear();
					l.add(next);
					rating = a;
				} else if (a == rating) {
					l.add(next);
				}
			}
		}
		
		if (l.size() > 0)
			return (Node) l.get(Settings.random.nextInt(l.size()));
		else
			return null;
	}
	
	/**
	 * Adds the specified socket connection as a server for this NodeGroup to communicate with.
	 * 
	 * @param s  Socket connection to server.
	 * @throws IOException  If an IO error occurs constructing a NodeProxy using the Socket.
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public synchronized boolean addServer(Socket s) throws IOException {
		return this.addServer(s, false);
	}
	
	public synchronized boolean addServer(Socket s, boolean server) throws IOException {
		try {
			return this.addServer(new NodeProxy(s, this.passwd, this.crypt, server));
		} catch (InvalidKeyException e) {
			System.out.println("\nNodeGroup: Invalid key (" + e.getMessage() + ").");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("\nNodeGroup: Encryption algorithm not found (" + e.getMessage() + ").");
		} catch (InvalidKeySpecException e) {
			System.out.println("\nNodeGroup: Invalid key spec (" + e.getMessage() + ").");
		} catch (NoSuchPaddingException e) {
			System.out.println("\nNodeGroup: Encryption padding not found (" + e.getMessage() + ").");
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println("\nNodeGroup: Invalid encryption parameter (" + e.getMessage() + ").");
		}
		
		return false;
	}
	
	public synchronized boolean addServer(NodeProxy pr) {
		this.connecting.add(pr);
		
		Iterator itr = this.servers.iterator();
		int d = 0;
		NodeProxy p = null;
		
		while (itr.hasNext()) {
			NodeProxy np = (NodeProxy) itr.next();
			
			if (np.equals(pr)) {
				d++;
				
				if (d == 1) p = np;
			}
		}
		
		if (d >= this.maxDuplicateConnections) {
			this.removeServer(p);
			this.displayMessage("Removed duplicate server " + p);
		}
		
		pr.addEventListener(this);
		
		synchronized (this.tasks) {
			Iterator titr = this.tasks.iterator();
			
			while (titr.hasNext()) {
				Object o = titr.next();
				if (o instanceof NodeProxy.EventListener) {
					pr.addEventListener((NodeProxy.EventListener) o);
				}
			}
		}
		
		pr.fireConnect();
		this.servers.add(pr);
		
		String msg = "Added server " + (this.servers.size() - 1);
		this.displayMessage(msg + " - " + pr);
		if (this.activityGraph != null)
			this.activityGraph.addMessage(msg);
		
		pr.flushQueue();
		
		this.connecting.remove(pr);
		
		return true;
	}
	
	/**
	 * Removes and disposes the connection between this node group and the peer
	 * with the specified index.
	 * 
	 * @param index  Index of peer to remove.
	 * @return  The total number of node connections dropped due to the removal.
	 */
	public synchronized int removeServer(int index) {
		return this.removeServer((NodeProxy)this.servers.get(index));
	}
	
	/**
	 * Removes and disposes the connection maintained by the specified NodeProxy object.
	 * 
	 * @param p  NodeProxy maintaing connection that is to be removed.
	 * @return  The total number of node connections dropped due to the removal.
	 */
	public synchronized int removeServer(NodeProxy p) {
		p.removeEventListener(this);
		
		int tot = 0;
		
		Iterator itr = NodeGroup.this.nodes.iterator();
		while (itr.hasNext()) tot += ((Node)itr.next()).disconnect(p);
		
		boolean r = this.servers.remove(p);
		
		if (tot > 0)
			this.displayMessage("Dropped " + tot + " connections to " + p);
		else if (r == true)
			this.displayMessage("Dropped server " + p);
		
		itr = this.plisteners.iterator();
		while (itr.hasNext()) ((NodeProxy.EventListener)itr.next()).disconnect(p);
		
		if (p.isConnected()) p.close();
		
		return tot;
	}
	
	public String[] getCurrentWork() {
		synchronized (this.nodes) {
			String w[] = new String[this.nodes.size()];
			Iterator itr = this.nodes.iterator();
			int i = 0;
			
			while (itr.hasNext()) {
				Object o = ((Node)itr.next()).getCurrentJob();
				if (o == null)
					w[i] = null;
				else
					w[i] = o.toString();
				
				i++;
			}
			
			return w;
		}
	}
	
	public NodeProxy[] getServers() {
		synchronized (this.servers) {
			return (NodeProxy[])this.servers.toArray(new NodeProxy[0]);
		}
	}
	
	/**
	 * Pings the specified peer.
	 * 
	 * @param peer  Index of peer to ping.
	 * @param size  Size of packet in characters (pairs of bytes).
	 * @param timeout  Max time to wait for a response, in milliseconds.
	 * @return  The time, in milliseconds, to respond to the ping.
	 */
	public synchronized long ping(int peer, int size, int timeout) throws IOException {
		return ((NodeProxy)this.servers.get(peer)).ping(size, timeout);
	}
	
	/**
	 * Selects a server at random and sends a connection request.
	 * This method may return null.
	 * 
	 * @param id  Unique id of this child node that is requesting the connection.
	 * @return  A Connection object that can be used to relay data bewteen a local node and a remote node.
	 */
	public synchronized Connection getConnection(int id) {
		NodeProxy p = null;
		
		w: while (true) {
			if (this.servers.size() < 1) return null;
			
			int s = (int)(Math.random() * this.servers.size());
			p = (NodeProxy)this.servers.get(s);
			
			if (p.isConnected())
				break w;
			else
				this.removeServer(p);
		}
		
		Connection c = null;
		
		try {
			Message m = new Message(Message.ConnectionRequest, id, p);
			m.setLocalNode((Node)this.nodes.get(id));
			c = (Connection)m.send(-1);
		} catch (SocketException se) {
			this.displayMessage("Removing server " + p + " (" + se.getMessage() + ")");
			this.removeServer(p);
		} catch (IOException ioe) {
			System.out.println("NodeGroup: " + ioe);
			return null;
		}
		
		// boolean b = c.confirm();
		
		return c;
	}
	
	protected JobFactory createTask(String data) {
		int index = data.indexOf(":");
		String className = data.substring(0, index);
		
		Class c = null;
		JobFactory j = null;
		
		try {
			c = Class.forName(className);
			j = (JobFactory)c.newInstance();
			
			boolean end = false;
			
			w: while (!end) {
				data = data.substring(index + 1);
				index = data.indexOf(":");
				
				while (data.charAt(index + 1) == '/' || (index > 0 && data.charAt(index - 1) == '\\'))
					index = data.indexOf(":", index + 1);
				
				String s = null;
				
				if (index <= 0) {
					s = data;
					end = true;
				} else {
					s = data.substring(0, index);
				}
				
				String key = s.substring(0, s.indexOf("="));
				String value = s.substring(s.indexOf("=") + 1);
				
				j.set(key, value);
			}
		} catch (ClassNotFoundException cnf) {
			System.out.println("NodeGroup: Class not found: " + className);
		} catch (ClassCastException cce) {
			System.out.println("NodeGroup: Error casting " + c.getName() + " to JobFactory");
		} catch (Exception e) {
			System.out.println("NodeGroup: " + e);
		}
		
		return j;
	}
	
	/**
	 * Adds the specified JobFactory object as a task for this NodeGroup.
	 * 
	 * @param f  JobFactory to use as task.
	 * @return  True if added, false otherwise.
	 */
	public boolean addTask(JobFactory f) {
		if (this.activityGraph != null)
			this.activityGraph.addMessage("Added task " + f.getTaskId());
		
		return this.tasks.add(f);
	}
	
	/**
	 * Constructs a JobFactory object and adds it as a task for this NodeGroup.
	 * 
	 * @param f  Encoded JobFactory to use as task.
	 * @return  True if added, false otherwise.
	 */
	public boolean addTask(String data) {
		JobFactory t = this.createTask(data);
		
		if (t == null) return false;
		
		if (super.getLog() != null && super.getLog().getLink() != null) {
			String n = this.getName();
			this.setName(n + "  Recieved task " + t);
			this.displayMessage("Recieved task " + t, super.getLog().getLink() +
								"NetworkRender-" + t.getTaskId() + ".jpg");
			this.setName(n);
		} else {
			this.displayMessage("Recieved task " + t);
		}
		
		return this.addTask(t);
	}
	
	/**
	 * Sends an encoded JobFactory instance to a server that this NodeGroup object
	 * is connected to.
	 * 
	 * @param data  Encoded JobFactory.
	 * @param server  Server index.
	 */
	public synchronized void sendTask(String data, int server) {
		try {
			Message m = new Message(Message.Task, -1, (NodeProxy)this.servers.get(server));
			m.setString(data);
			m.send(-1);
		} catch (IOException ioe) {
			System.out.println("NodeGroup: " + ioe);
		}
	}
	
	/**
	 * Sends an encoded JobFactory instance to a server that this NodeGroup object
	 * is connected to.
	 * 
	 * @param f  JobFactory to transmit.
	 * @param server  Server index.
	 */
	public synchronized void sendTask(JobFactory f, int server) {
		try {
			Message m = new Message(Message.Task, -1, (NodeProxy)this.servers.get(server));
			m.setString(f.encode());
			m.send(-1);
		} catch (IOException ioe) {
			System.out.println("NodeGroup: " + ioe);
		}
	}
	
	/**
	 * Sends a kill signal for the specified task to 
	 * 
	 * @param task  Task id to kill.
	 * @param relay  Number of times to relay the signal.
	 */
	public void sendKill(long task, int relay) {
		synchronized (this.tasks) {
			Iterator itr = this.tasks.iterator();
			while (itr.hasNext()) {
				if (((JobFactory)itr.next()).getTaskId() == task) {
					itr.remove();
					System.out.println("NodeGroup: Killed task " + task);
				}
			}
		}
		
		synchronized (this.nodes) {
			Iterator itr = this.nodes.iterator();
			while (itr.hasNext()) ((Node)itr.next()).sendKill(task, relay);
		}
	}
	
	/**
	 * Sets the component which will display the last status message printed by this node.
	 * 
	 * @param label  JLabel component to display status messages.
	 */
	public void setStatusLabel(JLabel label) {
		super.setStatusLabel(label);
		
		
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setStatusLabel(label);
	}
	
	public void setActivitySleepC(double acs) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setActivitySleepC(acs);
	}
	
	public void setPeerActivitySleepC(double pacs) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setPeerActivitySleepC(pacs);
	}
	
	public void setParentalRelayP(double parp) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setParentalRelayP(parp);
	}
	
	/**
	 * Sets the peer relay coefficient for each child of this node group
	 * to the specified double value.
	 */
	public void setPeerRelayC(double prc) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setPeerRelayC(prc);
	}
	
	public void setMinimumJobP(double mjp) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setMinimumJobP(mjp);
	}
	
	/**
	 * Sets the max number of failed jobs to be stored by each child
	 * of this node group.
	 */
	public void setMaxFailedJobs(int mfj) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setMaxFailedJobs(mfj);
	}
	
	/**
	 * Sets the relay probability (0.0 - 1.0) for each child of this node group
	 * to the specified double value.
	 */
	public void setRelayProbability(double r) {
		if (this.nodes == null) return;
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setRelayProbability(r);
	}
	
	/**
	 * @param w  True if peers to relay should be chosen using weighted probability.
	 */
	public void setWeightPeers(boolean w) {
		if (this.nodes == null) return;
		
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) ((Node)itr.next()).setWeightPeers(w);
	}
	
	/**
	 * @return  The number of jobs completed by all the children of this node.
	 */
	public int getCompletedJobCount() {
		int t = 0;
		
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) t += ((Node)itr.next()).getCompletedJobCount();
		
		return t;
	}
	
	/**
	 * @return  The total time the nodes in this node group have worked, measured in msecs.
	 */
	public double getTimeWorked() {
		double t = 0;
		
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) t += ((Node)itr.next()).getTimeWorked();
		
		return t - (t % 1);
	}
	
	public double getTimeCommunicated() {
		double t = 0;
		
		Iterator itr = this.nodes.iterator();
		while (itr.hasNext()) t += ((Node)itr.next()).getTimeCommunicated();
		
		return t - (t % 1);
	}
	
	/**
	 * @return  The average time for a node in this node group to complete a job.
	 *          (-1.0 if no jobs have been completed).
	 */
	public double getAverageJobTime() {
		Iterator itr = this.nodes.iterator();
		
		int i = 0;
		double tot = 0.0;
		
		while (itr.hasNext()) {
			Node n = (Node) itr.next();
			
			tot += n.getTimeWorked();
			i += n.getCompletedJobCount();
		}
		
		if (i == 0) {
			return -1.0;
		} else {
			return tot / i;
		}
	}
	
	/**
	 * @return  The average connectivity rating for the nodes in this node group.
	 */
	public double getAverageConnectivityRating() {
		Iterator itr = this.nodes.iterator();
		
		int i = 0;
		double tot = 0.0;
		
		while (itr.hasNext()) {
			tot += ((Node)itr.next()).getConnectivityRating();
			i++;
		}
		
		if (i == 0)
			return 0.0;
		else
			return tot / i;
	}
	
	/**
	 * @return  The value of this.getAverageActivityRating.
	 */
	public double getActivityRating() { return this.getAverageActivityRating(); }
	
	public double getAverageActivityRating() {
		Iterator itr = this.nodes.iterator();
		
		int i = 0;
		double tot = 0.0;
		
		while (itr.hasNext()) {
			tot += ((Node)itr.next()).getActivityRating();
			i++;
		}
		
		if (i == 0)
			return 0.0;
		else
			return tot / i + this.activityO;
	}
	
	/**
	 * @return  The ratio of the average activity rating reported by known servers to
	 *          the average node's activity rating.
	 */
	public double getPeerActivityRatio() {
		return this.getAveragePeerActivityRating() / this.getAverageActivityRating();
	}
	
	/**
	 * @return  The average activity rating reported by the servers connected
	 *          to this node group. (0.0 if no peers have reported an activity
	 *          rating measurement).
	 */
	public double getAveragePeerActivityRating() {
		NodeProxy p[] = this.getServers();
		
		double sum = 0.0;
		int peers = 0;
		
		for (int i = 0; i < p.length; i++) {
			double j = p[i].getActivityRating();
			if (j > 0) {
				sum += j;
				peers++;
			}
		}
		
		if (peers > 0) {
			return sum / peers;
		} else {
			return 0.0;
		}
	}
	
	/**
	 * Prints the status of this network node group to standard out.
	 */
	public void printStatus() { this.printStatus(System.out); }
	
	/**
	 * Prints the status of this network node group using the specified PrintStream object.
	 * 
	 * @param out  PrintStream to use.
	 */
	public void printStatus(PrintStream out) {
		out.println("<html>");
		out.println("<head><title>");
		out.println("Node Group Status");
		out.println("</title></head><body>");
		out.println(this.getStatus("<br>\n"));
		out.println("</body></html>");
		
		// HtmlFormat.printPage(out, "Node Group Status", this.getStatus("<br>\n"));
	}
	
	/**
	 * @return  A String containing status information for this network node group.
	 *          The string is formatted with HTML.
	 */
	public String getStatus(String nl) {
		if (Message.verbose) System.out.println("NodeGroup: Starting status check.");
		
		StringBuffer buf = new StringBuffer();
		
		Date now = new Date();
		
		buf.append(now.toString() + nl + nl);
		
		buf.append("<center><h1>Network Node Group Status</h1>");
		buf.append("<p><h3>" + this.toString() + "</h3>" + nl);
		buf.append("<b>Sleep time:</b> " + Node.formatTime(super.getSleep()) + "</p></center>" + nl);
		
		NodeProxy s[] = this.getServers();
		if (Message.verbose) System.out.println("NodeGroup.getStatus: Got server list.");
		
		buf.append("<table><tr><td><h3>Servers</h3></td><td><h3>TaskList</h3></td></tr><tr>");
		
		buf.append("<td>");
		
		for (int i = 0; i < s.length; i++)
			buf.append("\t" + s[i].toString(true) + nl);
		
		buf.append("</td><td>");
		
		Iterator itr;
		
		itr = ((List)((ArrayList)this.tasks).clone()).iterator();
		while (itr.hasNext())
			buf.append("\t" + ((JobFactory)itr.next()).getName() + nl);
		
		buf.append("</td></tr></table>");
		
		itr = this.nodes.iterator();
		while (itr.hasNext()) buf.append(((Node)itr.next()).getStatus(nl));
		
		buf.append(nl);
		
		if (this.activityGraph != null) {
			double a = 0.0;
			
			if (this.activityDivisor > 0) {
				a = this.activitySum / this.activityDivisor;
				this.activitySum = 0.0;
				this.activityDivisor = 0;
			} else {
				a = this.getActivityRating();
			}
			
			this.activityGraph.addEntry(a);
		}
		
		if (this.sleepGraph != null) {
			double sl = 0.0;
			
			if (this.sleepDiv > 0) {
				sl = this.sleepSum / this.sleepDiv;
				this.sleepSum = 0.0;
				this.sleepDiv = 0;
			} else {
				sl = this.getSleep();
			}
			
			this.sleepGraph.addEntry(sl);
		}
		
		buf.append("<b>Activity Rating</b>" + nl);
		buf.append("Running Total Average = ");
		buf.append(this.totalActivitySum / this.totalActivityDiv);
		buf.append(nl);
		buf.append("<pre><font size=\"-2\">" + nl);
		this.activityGraph.print(buf);
		buf.append("</font></pre>" + nl);
		
		if (Message.verbose) System.out.println("NodeGroup: Getting dbs info...");
		
		com.almostrealism.flow.db.OutputServer dbs = com.almostrealism.flow.db.OutputServer.getCurrentServer();
		if (dbs != null) {
			if (this.tpLast % this.tpFreq == 0) {
				this.throughputGraph.addEntry(dbs.getThroughput());
				this.tpLast = 1;
			} else {
				this.tpLast++;
			}
			
			synchronized (dbs) {
				buf.append("<b>DBS Throughput</b>" + nl);
				buf.append("Running Total Average = ");
				buf.append(Node.dFormat.format(dbs.getTotalAverageThroughput()));
				buf.append(" jobs per minute.");
				buf.append(nl);
				buf.append("Average Job Time = ");
				buf.append(Node.dFormat.format(dbs.getTotalAverageJobTime() / 60000.0));
				buf.append(" minutes per job.");
				buf.append(nl);
				buf.append("<pre><font size=\"-2\">" + nl);
				this.throughputGraph.print(buf);
				buf.append("</font></pre>" + nl);
			}
		}
		
		if (Message.verbose) System.out.println("NodeGroup: Returning status check.");
		
		return buf.toString();
	}
	
	/**
	 * Stores the elements in the activity rating graph maintained by this node group.
	 * The data will be output by the Graph class (net.sf.j3d.util) and the format should
	 * be newline separated decimal values, with each new line representing a uniform
	 * increment of time.
	 * 
	 * @param f  File representing location to store activity rating data.
	 * @return  True if the file was written, false if no activity data is being collected.
	 * @throws IOException  If an IO error occurs writing the file.
	 */
	public boolean storeActivityGraph(File f) throws IOException {
		if (this.activityGraph != null) {
			this.activityGraph.storeValues(f);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Stores the elements in the sleep time graph maintained by this node group.
	 * The data will be output by the Graph class (net.sf.j3d.util) and the format should
	 * be newline separated integer values, with each new line representing a uniform
	 * increment of time.
	 * 
	 * @param f  File representing location to store sleep time data.
	 * @return  True if the file was written, false if no sleep time data is being collected.
	 * @throws IOException  If an IO error occurs writing the file.
	 */
	public boolean storeSleepGraph(File f) throws IOException {
		if (this.sleepGraph != null) {
			this.sleepGraph.storeValues(f);
			return true;
		}
		
		return false;
	}
	
	public void run() {
		while (!this.stop) {
			this.iteration(this);
			
			int svrs = this.servers.size();
			
			try {
				int sleep = super.getSleep();
				
				if (svrs > 0 || this.tasks.size() > 0)
					Thread.sleep(sleep);
				else
					Thread.sleep(sleep * 10);
				
//				double aar = this.getAverageActivityRating();
//				this.activitySum += aar;
//				this.totalActivitySum += aar;
//				this.activityDivisor++;
//				this.totalActivityDiv++;
			} catch (InterruptedException ie) {
				System.out.println("NodeGroup: " + ie);
			}
			
			if (this.isolationTime > 200) {
				this.becameIsolated();
			} else if (svrs == 0) {
				this.isolationTime++;
			} else {
				this.isolationTime = 0;
			}
			
			Iterator itr = ((List)((ArrayList)this.tasks).clone()).iterator();
			
			Job j = this.nextJob();
			if (j == null) this.jobWasNull++;
			
			if (this.verbose && j != null) {
				if (this.jobWasNull > 1) {
					System.out.println("NodeGroup: Last " +
									(this.jobWasNull - 1) +
									" jobs were null.");
				}
				
				System.out.println("NodeGroup: nextJob = " + j);
			}
			
			if (this.jobWasNull % 21 == 0) {
				if (this.verbose)
					System.out.println("NodeGroup: Last " +
									(this.jobWasNull - 1) +
									" jobs were null.");
				
				this.jobWasNull = 1;
			}
			
			if (j != null) this.getLeastActiveNode().addJob(j);
			
			i: for (int i = 0; itr.hasNext() && i < this.maxTasks; i++) {
				JobFactory f = (JobFactory) itr.next();
				
				if (f.isComplete()) {
					this.tasks.remove(i);
					continue i;
				}
				
				double t = this.jobsPerTask * f.getPriority();
				
				for (int k = 0; k < t; k++) {
					try {
						j = f.nextJob();
					} catch (RuntimeException e) {
						System.out.println("NodeGroup: Runtime exception while getting next job from " + f);
						
						if (e.getCause() != null)
							e.getCause().printStackTrace();
						else
							e.printStackTrace();
					}
					
					if (j != null) {
						if (this.verbose)
							System.out.println("NodeGroup: " + f + "  nextJob = " + j);
						
						Node n = this.getLeastActiveNode();
						if (n != null) n.addJob(j);
					}
				}
			}
		}
	}
	
	public void addProxyEventListener(NodeProxy.EventListener l) { this.plisteners.add(l); }
	
	/**
	 * @see com.almostrealism.flow.NodeProxy.EventListener#connect(com.almostrealism.flow.NodeProxy)
	 */
	public void connect(NodeProxy pr) {
		if (this.connecting.contains(pr)) return;
		this.addServer(pr);
	}
	
	/**
	 * @see com.almostrealism.flow.NodeProxy.EventListener#disconnect(com.almostrealism.flow.NodeProxy)
	 * @return  The number of connections dropped.
	 */
	public int disconnect(NodeProxy p) { return this.removeServer(p); }
	
	/**
	 * @see com.almostrealism.flow.NodeProxy.EventListener#recievedMessage(com.almostrealism.flow.Message, int)
	 */
	public boolean recievedMessage(Message m, int reciever) {
		if (reciever == -1) {
			NodeProxy p = m.getNodeProxy();
			
			int type = m.getType();
			int remoteId = m.getSender();
			
			if (type == Message.Job) {
				System.out.println("NodeGroup: Recieved job. Data = " + m.getData());
				this.getLeastActiveNode().addJob(this.defaultFactory.createJob(m.getData()));
			} else if (type == Message.StringMessage) {
				System.out.println("Message from " + p.toString() + ": " + m.getData());
			} else if (type == Message.ConnectionRequest) {
				try {
					Node n = this.getLeastConnectedNode();
					Connection c;
					
					if (n != null && n.getPeers().length < n.getMaxPeers() && !n.isConnected(p)) {
						System.out.println("NodeGroup: Constructing connection...");
						c = new Connection(n, p, remoteId);
					} else {
						c = null;
					}
					
					if (c != null && n.connect(c)) {
						Message response = new Message(Message.ConnectionConfirmation, n.getId(), p);
						response.setString("true");
						response.send(remoteId);
					} else {
					//	Message response = new Message(-1, -1, p);
					//	response.setString("false");
					//	response.send(remoteId);
					}
				} catch (IOException ioe) {
					System.out.println("NodeGroup: " + ioe);
				}
			} else if (type == Message.ConnectionConfirmation) {
				if (m.getData() == null) {
					try {
						Message response = new Message(Message.ConnectionConfirmation, -1, p);
						response.setString("true");
						response.send(remoteId);
					} catch (IOException ioe) {
						System.out.println("NodeGroup: " + ioe);
					}
				}
			} else if (type == Message.ServerStatus) {
				String s[] = m.getData().split(";");
				
				boolean h = false;
				
				for (int i = 0; i < s.length; i++) {
					int index = s[i].indexOf(":");
					String v = "";
					if (index > 0 && index < s[i].length() - 1) v = s[i].substring(index + 1);
					
					try {
						if (s[i].startsWith("jobtime:")) {
							p.setJobTime(Double.parseDouble(v));
							h = true;
						} else if (s[i].startsWith("activity:")) {
							p.setActivityRating(Double.parseDouble(v));
							h = true;
						} else {
							System.out.println("NodeGroup: Unknown status type '" + s[i] + "'");
						}
					} catch (NumberFormatException nfe) {
						System.out.println("NodeGroup: Could not parse status item '" +
											s[i] + "' (" + nfe.getMessage() + ")");
					}
				}
				
				if (!h) return false;
			} else if (type == Message.ServerStatusQuery) {
				if (m.getData().equals("peers")) {
					try {
						Message response = new Message(Message.ServerStatus, -1, p);
						
						NodeProxy svs[] = this.getServers();
						
						StringBuffer b = new StringBuffer();
						b.append("peers:");
						boolean f = false;
						int j = 0;
						for (int i = 0; i < svs.length; i++) {
							if (svs[i] != p) {
								if (f) {
									b.append("," + svs[i]);
								} else {
									b.append(svs[i]);
									f = true;
								}
							} else {
								j++;
							}
						}
						
						if (Message.verbose)
							System.out.println("NodeGroup: Reported " + (svs.length - j) +
												" peers for status query (Excluded " + p + ").");
						
						response.setString(b.toString());
						response.send(remoteId);
					} catch (IOException ioe) {
						System.out.println("NodeGroup: Error sending server status (" +
											ioe.getMessage() + ")");
					}
				}
			} else if (type == Message.ResourceRequest) {
				try {
					Message response = new Message(Message.ResourceUri, -1, p);
					
					Server s = Client.getCurrentClient().getServer();
					String r = s.getResourceUri(m.getData());
					System.out.println("NodeGroup: Sending resource uri (" + r + ")");
					response.setString(r);
					response.send(remoteId);
				} catch (IOException ioe) {
					System.out.println("NodeGroup: Error sending resource uri (" +
										ioe.getMessage() + ")");
				}
			} else if (type == Message.Task) {
				if (m.getData() != null)
					this.addTask(m.getData());
				else
					this.displayMessage("Recieved null task.");
			} else if (type == Message.Kill) {
				int i = m.getData().indexOf(":");
				long task = Long.parseLong(m.getData().substring(0, i));
				int relay = Integer.parseInt(m.getData().substring(i + 1));
				
				this.sendKill(task, relay--);
			} else {
				return false;
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Network Node Group: ");
		
		int nodes = 0;
		
		if (this.nodes != null) nodes = this.nodes.size();
		
		if (nodes > 0) {
			b.append(nodes);
			b.append(" child");
			if (nodes > 1) b.append("ren");
		}
		
		int servers = 0;
		
		if (this.servers != null) servers = this.servers.size();
		
		if (servers > 0) {
			if (nodes > 0) b.append(" and ");
			b.append(servers);
			b.append(" server connection");
			if (servers > 1) b.append("s");
		}
		
		b.append(".");
		
		int jobs = 0;
		if (this.jobs != null) jobs = this.jobs.size();
		
		if (jobs > 0) {
			b.append(" ");
			b.append(jobs);
			b.append(" jobs in queue.");
		}
		
		return b.toString();
	}
	
	public void iteration(Node n) {
		n.setSleep((int) (n.getActivityRating() * n.getSleep()));
		
		if (this.verbose)
			System.out.println("NodeGroup: Notifying iteration.");
		
		synchronized (this.listeners) {
			Iterator itr = this.listeners.iterator();
			while (itr.hasNext()) ((ActivityListener)itr.next()).iteration(this);
		}
		
		if (this.verbose)
			System.out.println("NodeGroup: Notified listeners.");
	}
}
