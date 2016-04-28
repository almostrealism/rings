/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.raytracer.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.j3d.network.Job;
import net.sf.j3d.network.db.Client;
import net.sf.j3d.network.db.JobOutput;
import net.sf.j3d.network.db.OutputHandler;
import net.sf.j3d.network.db.Query;
import net.sf.j3d.network.db.QueryHandler;
import net.sf.j3d.io.FileDecoder;
import net.sf.j3d.io.FileEncoder;
import net.sf.j3d.io.FilePrintWriter;
import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.shaders.DiffuseShader;
import net.sf.j3d.run.Settings;
import net.sf.j3d.util.graphics.RGB;

/**
 * A RayTracingJob object provides an implementation of
 * net.sf.j3d.network.Job that renders a section of an image.
 * 
 * @author Mike Murray
 */
public class RayTracingJob implements Job, SceneLoader {
	public static final String htmlPre = "<html> <head> <title>Universe in a Box</title> </head> " +
										"<body bgcolor=\"#000000\" text=\"#ffffff\"> <center> " +
										"<h1>Universe in a Box</h1> <img src=\"images/NetworkRender-";
	public static final String htmlPost = ".jpg\"/> </center> </body> </html>";
	
	public static boolean verboseRender = false;
	public boolean local = false;
	
	private static RayTracingOutputHandler defaultOutputHandler;
	
	public static class RayTracingOutputHandler implements OutputHandler,
														QueryHandler {
		private static List completedTasks;
		
		private RGB image[][];
		private long taskId;
		
		private Set children;
		private long lastTaskId, currentTaskId;
		private boolean recievedQuery;
		
		public RayTracingOutputHandler() { this(-1, 0, 0); }
		
		public RayTracingOutputHandler(long id, int w, int h) {
			System.out.println("Constructing RayTracingOutputHandler " + this.hashCode() + ": " + id + " " + w + " " + h);
			
			if (RayTracingOutputHandler.completedTasks == null)
				RayTracingOutputHandler.completedTasks = new ArrayList();
			
			this.taskId = id;
			
			if (this.taskId == -1) {
				this.children = new HashSet();
				RayTracingJob.defaultOutputHandler = this;
			} else {
				this.image = new RGB[w][h];
				
				Thread t = new Thread(new Runnable() {
					public void run() {
						if (RayTracingOutputHandler.this.taskId == -1) return;
						
						System.out.println("RayTracingJobOutputHandler: Started file output thread for " +
											RayTracingOutputHandler.this.taskId + ".");
						
						w: while (!RayTracingOutputHandler.this.isComplete()) {
							try {
								Thread.sleep(1200000);
								
								if (RayTracingOutputHandler.this.image.length <= 0) continue w;
								
								System.out.println("RayTracingJobOutputHandler: Writing image for task " +
													RayTracingOutputHandler.this.taskId + " (" + 
													RayTracingOutputHandler.this.image.length + ", " + 
													RayTracingOutputHandler.this.image[0].length + ")...");
								
								PrintStream p = new PrintStream(new FileOutputStream(
											"images/NetworkRender-" +
											RayTracingOutputHandler.this.taskId + ".raw"));
								
								for (int i = 0; i < RayTracingOutputHandler.this.image.length; i++) {
									for (int j = 0; j < RayTracingOutputHandler.this.image[i].length; j++) {
										p.println("[" + i + ", " + j + "]: " + RayTracingOutputHandler.this.image[i][j]);
									}
								}
								
								p.flush();
								p.close();
								
								FileEncoder.encodeImageFile(RayTracingOutputHandler.this.getImage(),
										new File("images/NetworkRender-" + RayTracingOutputHandler.this.taskId + ".jpg"),
										FileEncoder.JPEGEncoding);
							} catch (InterruptedException ie) {
								System.out.println("RayTracingOutputHandler: " + ie);
							} catch (IOException ioe) {
								System.out.println("RayTracingOutputHandler: " + ioe);
							}
						}
						
						if (RayTracingJob.defaultOutputHandler.children.remove(
								RayTracingOutputHandler.this))
							System.out.println("RayTracingOutputHandler (" +
									RayTracingOutputHandler.this.taskId +
									" Task is complete.");
						
						RayTracingOutputHandler.completedTasks.add(
								new Long(RayTracingOutputHandler.this.taskId));
					}
				});
				
				t.setName("Ray Tracing Output Handler Thread for " + this.taskId);
				
				t.start();
			}
		}
		
		public void writeImage() {
			try {
				if (RayTracingOutputHandler.this.image.length <= 0) return;
				
				System.out.println("RayTracingOutputHandler: Writing image for task " +
									RayTracingOutputHandler.this.taskId + " (" + 
									RayTracingOutputHandler.this.image.length + ", " + 
									RayTracingOutputHandler.this.image[0].length + ")...");
				
				PrintStream p = new PrintStream(new FileOutputStream(
							"NetworkRender-" + RayTracingOutputHandler.this.taskId + ".raw"));
				
				for (int i = 0; i < RayTracingOutputHandler.this.image.length; i++) {
					for (int j = 0; j < RayTracingOutputHandler.this.image[i].length; j++) {
						p.println("[" + i + ", " + j + "]: " + RayTracingOutputHandler.this.image[i][j]);
					}
				}
				
				p.flush();
				p.close();
				
				FileEncoder.encodeImageFile(RayTracingOutputHandler.this.getImage(),
						new File("images/NetworkRender-" + RayTracingOutputHandler.this.taskId + ".jpg"),
						FileEncoder.JPEGEncoding);
			} catch (IOException ioe) {
				System.out.println("RayTracingJobOutputHandler: " + ioe);
			}
		}
		
		public RayTracingOutputHandler getHandler(long task) {
			if (this.taskId == task) return this;
			if (this.children == null) return null;
			
			Iterator itr = this.children.iterator();
			while (itr.hasNext()) {
				RayTracingOutputHandler h = ((RayTracingOutputHandler)itr.next()).getHandler(task);
				if (h != null) return h;
			}
			
			return null;
		}
		
		public long getId() { return this.taskId; }
		
		public boolean isComplete() {
			if (this.taskId == -1 || !this.recievedQuery) return false;
			
			for (int i = 0; i < this.image.length; i++) {
				for (int j = 0; j < this.image[i].length; j++) {
					if (this.image[i][j] == null) return false;
				}
			}
			
			return true;
		}
		
		public void storeOutput(long time, int uid, JobOutput data) {
			if (data instanceof RayTracingJobOutput == false) {
				System.out.println("RayTracingOutputHandler (" + this.taskId + ") recieved: " + data);
				return;
			}
			
			RayTracingJobOutput output = (RayTracingJobOutput) data;
			
			if (output.getTaskId() <= 0) {
				System.out.println("RayTracingOutputHandler (" + this.taskId + ") recieved: " + data);
				return;
			}
			
			t: if (this.taskId == -1) {
				Iterator itr = this.children.iterator();
				
				long id = output.getTaskId();
				
				while (itr.hasNext()) {
					RayTracingOutputHandler h = (RayTracingOutputHandler) itr.next();
					
					if (h.isComplete()) itr.remove();
					
					if (h.getId() == id) {
						h.storeOutput(time, uid, data);
						break t;
					}
				}
				
				System.out.println("RayTracingOutputHandler: Recieved " + output);
				System.out.println("RayTracingOutputHandler: Spawning Output Handler for job " + id + "...");
				
				RayTracingOutputHandler h = new RayTracingOutputHandler(id, output.getDx(), output.getDy());
				h.storeOutput(time, uid, data);
				
				this.children.add(h);
				
				this.lastTaskId = this.currentTaskId;
				this.currentTaskId = id;
				
				try {
					System.out.println("RayTracingOutputHandler: Writing index.html");
					
					String s = RayTracingJob.htmlPre + this.lastTaskId + RayTracingJob.htmlPost;
					
					PrintStream out = new PrintStream(new FileOutputStream("index.html"));
					out.println(s);
					out.flush();
					out.close();
				} catch (IOException ioe) {
					System.out.println("RayTracingOutputHandler: IO error writing index.html (" +
										ioe.getMessage() + ")");
				}
			}
			
			this.addToImage(output, output.getX(), output.getY(), output.getDx(), output.getDy());
		}
		
		public Hashtable executeQuery(Query q) {
			if (this.taskId == -1) {
				Iterator itr = RayTracingOutputHandler.completedTasks.iterator();
				
				while (itr.hasNext()) {
					if (q.getTable().equals("image-" + itr.next()))
						return new Hashtable();
				}
				
				itr = this.children.iterator();
				
				while (itr.hasNext()) {
					Hashtable h = ((RayTracingOutputHandler)itr.next()).executeQuery(q);
					if (h != null) return h;
				}
				
				System.out.println("RayTracingJobOutputHandler: Recieved query for " +
						q.getTable() + " (" + q.getCondition() + ")");
			}
			
			if (!q.getTable().equals("image-" + this.taskId)) return null;
			
			System.out.println("RayTracingJobOutputHandler: Recieved query for " +
					q.getTable() + " (" + q.getCondition() + ")");
			
			Hashtable result = new Hashtable();
			
			int n = 0;
			
			int index = q.getCondition().indexOf("x");
			int w = Integer.parseInt(q.getCondition().substring(0, index));
			int h = Integer.parseInt(q.getCondition().substring(index + 1));
			
			this.expandImageBuffer(w, h);
			
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					try {
						if (this.image[i][j] == null) {
							result.put(new Integer(n++), i + ":" + j);
						}
					} catch (ArrayIndexOutOfBoundsException oob) {
						System.out.println("RayTracingJobOutputHandler (" + this.taskId + "): " + oob);
						oob.printStackTrace(System.out);
						result.put(new Integer(n++), i + ":" + j);
					}
				}
			}
			
			System.out.println("RayTracingJobOutputHandler (" + this.taskId +
					"): Found " + n + " null pixels.");
			
			this.recievedQuery = true;
			
			return result;
		}
		
		public void expandImageBuffer(int w, int h) {
			if (w != this.image.length || this.image.length <= 0 || h != this.image[0].length) {
				RGB copy[][] = new RGB[w][h];
				
				for (int i = 0; i < this.image.length; i++)
					for (int j = 0; j < this.image[i].length; j++)
						copy[i][j] = this.image[i][j];
				
				this.image = copy;
				
				// System.out.println("RayTracingOutputHandler (" + this.taskId + ") expanded image buffer to: " + w + " " + h);
			}
		}
		
		protected synchronized void addToImage(RayTracingJobOutput data, int x, int y, int dx, int dy) {
			if (this.taskId == -1) return;
			if (this.image == null) this.image = new RGB[0][0];
			
			int w = this.image.length, h;
			
			if (w <= 0)
				h = 0;
			else
				h = this.image[0].length;
			
			if (x >= w) w = x + dx;
			if (y >= h) h = y + dy;
			
			this.expandImageBuffer(w, h);
			
			this.image = RayTracingJob.processOutput(data, this.image, x, y, dx, dy);
		}
		
		public synchronized RGB[][] getImage() {
			RGB copy[][] = new RGB[this.image.length][this.image[0].length];
			
			for (int i = 0; i < copy.length; i++) {
				for (int j = 0; j < copy[i].length; j++) {
					if (this.image[i][j] == null)
						copy[i][j] = new RGB(0.0, 0.0, 0.0);
					else
						copy[i][j] = (RGB) this.image[i][j].clone();
				}
			}
			
			return copy;
		}
	}
	
  private static Map scenes;
  private static List loading;
  
  private String sceneUri, sLoader;
  private int x, y, dx, dy, w, h, ssw, ssh;
  private long jobId;
  private double pw = -1.0, ph = -1.0;
  private double fl = -1.0;
  private double clx, cly, clz;
  private double cdx, cdy, cdz;

	/**
	 * Constructs a new RayTracingJob object.
	 */
	public RayTracingJob() {
	    if (RayTracingJob.scenes == null)
	    		RayTracingJob.scenes = Collections.synchronizedMap(new Hashtable());
	    
	    if (RayTracingJob.loading == null)
	    		RayTracingJob.loading = Collections.synchronizedList(new ArrayList());
	    
	}
	
	/**
	 * Constructs a new RayTracingJob object.
	 * 
	 * @param sceneUri  URI pointing to XML scene data.
	 * @param x  X coordinate of upper left corner of the section to be rendered.
	 * @param y  Y coordinate of upper left corner of the section to be rendered.
	 * @param dx  Width of section to be rendered.
	 * @param dy  Height of section to be rendered.
	 * @param w  Width of whole image.
	 * @param h  Height of whole image.
	 * @param ssw  Supersample width.
	 * @param ssh  Supersample height.
	 * @param jobId  Unique id for this job (often the time in ms is used)
	 */
	public RayTracingJob(String sceneUri, int x, int y, int dx, int dy, int w, int h,
						int ssw, int ssh, long jobId) {
		if (RayTracingJob.scenes == null) RayTracingJob.scenes = new Hashtable();
		if (RayTracingJob.loading == null) RayTracingJob.loading = new ArrayList();
		
		this.sceneUri = sceneUri;
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.w = w;
		this.h = h;
		this.ssw = ssw;
		this.ssh = ssh;
		this.jobId = jobId;
	}
	
	public static RayTracingOutputHandler getDefaultOutputHandler() {
		return RayTracingJob.defaultOutputHandler;
	}
	
	public static RGB[][] processOutput(RayTracingJobOutput data, RGB image[][], int x, int y, int dx, int dy) {
		Iterator itr = data.iterator();
		
		j: for (int j = 0; itr.hasNext() ; j++) {
			int ax = x + j % dx;
			int ay = y + j / dx;
			
			try {
				RGB rgb = (RGB) itr.next();
				
				if (image[ax][ay] != null)
					System.out.println("RayTracingJob.processOutput (" + j + "): " +
							"Duplicate pixel data at " + ax + ", " + ay + " = " +
							image[ax][ay] + " -- " + rgb);
				
				image[ax][ay] = rgb;
			} catch (ArrayIndexOutOfBoundsException obe) {
				System.out.println("RayTracingJob.processOutput (" +
									image.length + ", " + image[0].length +
									"  " + ax + ", " + ay + "): " + obe);
			}
		}
		
		return image;
	}
	
	public static boolean removeSceneCache(String s) { return(RayTracingJob.scenes.remove(s) != null); }
	
	/**
	 * @return  The scene referenced by this RayTracingJob object.
	 */
	public Scene getScene() {
		Scene s = null;
		
		i: for (int i = 0;;) {
			s = (Scene)RayTracingJob.scenes.get(this.sceneUri);
			
			if (RayTracingJob.loading.contains(this.sceneUri)) {
				
				try {
					int sleep = 1000;
					
					if (i == 0) {
						sleep = 1000;
						i++;
					} else if (i == 1) {
						sleep = 5000;
						i++;
					} else if (i == 2) {
						sleep = 10000;
						i++;
					} else if (i < 6) {
						sleep = 10000 * (int) Math.pow(2, i);
						i++;
					} else {
						sleep = 1200000;
					}
					
					Thread.sleep(sleep);
					
					System.out.println("RayTracingJob: Waited " + sleep / 1000.0 +
										" seconds for " + this.sceneUri);
				} catch (InterruptedException ie) {}
			} else if (s == null) {
				try {
					this.loading.add(this.sceneUri);
					
					SceneLoader loader = this;
					
					if (this.sLoader != null) {
						Object l = Class.forName(this.sLoader).newInstance();
						
						if (l instanceof SceneLoader)
							loader = (SceneLoader) l;
						else
							System.out.println("RayTracingJob: " + this.sLoader +
												" is not a valid SceneLoader.");
					}
					
					System.out.println("RayTracingJob: Loading scene from " +
										this.sceneUri + " via " + loader);
					
					s = loader.loadScene(this.sceneUri);
					if (s == null) throw new IOException();
					
					System.out.println("RayTracingJob: Scene loaded.");
					
					RayTracingJob.scenes.put(this.sceneUri, s);
				} catch (IOException ioe) {
					System.out.println("RayTracingJob: Error loading scene - " + ioe);
				} catch (InstantiationException e) {
					System.out.println("RayTracingJob: Unable to instantiate scene loader (" +
										e.getMessage() + ")");
				} catch (IllegalAccessException e) {
					System.out.println("RayTracingJob: Illegal access to scene loader (" +
										e.getMessage() + ")");
				} catch (ClassNotFoundException e) {
					System.out.println("RayTracingJob: Scene loader (" + this.sLoader +
										") not found.");
				}
				
				this.loading.remove(this.sceneUri);
				break i;
			} else {
				this.loading.remove(this.sceneUri);
				return s;
			}
		}
		
		return s;
	}
	
	public Scene loadScene(String uri) throws MalformedURLException, IOException {
		InputStream in;
		
		if (this.local) {
			in = (new URL(uri)).openStream();
		} else {
			in = Client.getCurrentClient().getServer().loadResource(uri).getInputStream();
		}
		
		return FileDecoder.decodeScene(in, FileDecoder.XMLEncoding, false, null);
	}
	
	public void setSceneLoader(String loader) { this.sLoader = loader; }
	public void setProjectionWidth(double w) { this.pw = w; }
	public void setProjectionHeight(double h) { this.ph = h; }
	public void setFocalLength(double f) { this.fl = f; }
	
	public void setCameraLocation(double x, double y, double z) {
		this.clx = x;
		this.cly = y;
		this.clz = z;
	}
	
	public void setCameraDirection(double x, double y, double z) {
		this.cdx = x;
		this.cdy = y;
		this.cdz = z;
	}
	
	/**
	 * @see net.sf.j3d.network.Job#encode()
	 */
	public String encode() {
		StringBuffer s = new StringBuffer();
		
		s.append(this.getClass().getName());
		s.append(":uri=");
		s.append(this.sceneUri);
		
		if (this.sLoader != null) {
			s.append(":sl=");
			s.append(this.sLoader);
		}
		
		s.append(":x=");
		s.append(this.x);
		s.append(":y=");
		s.append(this.y);
		s.append(":dx=");
		s.append(this.dx);
		s.append(":dy=");
		s.append(this.dy);
		s.append(":w=");
		s.append(this.w);
		s.append(":h=");
		s.append(this.h);
		s.append(":ssw=");
		s.append(this.ssw);
		s.append(":ssh=");
		s.append(this.ssh);
		s.append(":id=");
		s.append(this.jobId);
		
		if (this.pw != -1) {
			s.append(":pw=");
			s.append(this.pw);
		}
		
		if (this.ph != -1) {
			s.append(":ph=");
			s.append(this.ph);
		}
		
		if (this.fl != -1) {
			s.append(":fl=");
			s.append(this.fl);
		}
		
		if (this.clx != 0) {
			s.append(":clx=");
			s.append(this.clx);
		}
		
		if (this.cly != 0) {
			s.append(":cly=");
			s.append(this.cly);
		}
		
		if (this.clz != 0) {
			s.append(":clz=");
			s.append(this.clz);
		}
		
		if (this.cdx != 0) {
			s.append(":cdx=");
			s.append(this.cdx);
		}
		
		if (this.cdy != 0) {
			s.append(":cdy=");
			s.append(this.cdy);
		}
		
		if (this.cdz != 0) {
			s.append(":cdz=");
			s.append(this.cdz);
		}
		
		return s.toString();
	}
	
	/**
	 * @see net.sf.j3d.network.Job#set(java.lang.String, java.lang.String)
	 */
	public void set(String key, String value) {
		if (key.equals("uri"))
			this.sceneUri = value;
		else if (key.equals("sl"))
			this.sLoader = value;
		else if (key.equals("x"))
			this.x = Integer.parseInt(value);
		else if (key.equals("y"))
			this.y = Integer.parseInt(value);
		else if (key.equals("dx"))
			this.dx = Integer.parseInt(value);
		else if (key.equals("dy"))
			this.dy = Integer.parseInt(value);
		else if (key.equals("w"))
			this.w = Integer.parseInt(value);
		else if (key.equals("h"))
			this.h = Integer.parseInt(value);
		else if (key.equals("ssw"))
			this.ssw = Integer.parseInt(value);
		else if (key.equals("ssh"))
			this.ssh = Integer.parseInt(value);
		else if (key.equals("id"))
			this.jobId = Long.parseLong(value);
		else if (key.equals("pw"))
			this.pw = Double.parseDouble(value);
		else if (key.equals("ph"))
			this.ph = Double.parseDouble(value);
		else if (key.equals("fl"))
			this.fl = Double.parseDouble(value);
		else if (key.equals("clx"))
			this.clx = Double.parseDouble(value);
		else if (key.equals("cly"))
			this.cly = Double.parseDouble(value);
		else if (key.equals("clz"))
			this.clz = Double.parseDouble(value);
		else if (key.equals("cdx"))
			this.cdx = Double.parseDouble(value);
		else if (key.equals("cdy"))
			this.cdy = Double.parseDouble(value);
		else if (key.equals("cdz"))
			this.cdz = Double.parseDouble(value);
		else
			return;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Client c = Client.getCurrentClient();
		Scene s = this.getScene();
		
		if (RayTracingJob.verboseRender)
			System.out.println("Got scene: " + s);
		
		if (s == null) {
			System.out.println("RayTracingJob: No scene data available.");
			return;
		}
		
		if (RayTracingJob.verboseRender && !Settings.produceOutput) {
			try {
				Settings.produceOutput = true;
				Settings.produceRayTracingEngineOutput = true;
				Settings.rayEngineOut = new FilePrintWriter(new File("raytracer.out"));
				Settings.produceShaderOutput = true;
				Settings.shaderOut = new FilePrintWriter(new File("shaders.out"));
				DiffuseShader.produceOutput = true;
			} catch (FileNotFoundException fnf) {
				fnf.printStackTrace();
			}
		}
		
		if (RayTracingJob.verboseRender)
			System.out.println("Rendering Scene...");
		
		Camera camera = s.getCamera();
		
		if (camera instanceof PinholeCamera && (
				this.pw != -1 ||
				this.ph != -1 ||
				this.fl != -1 ||
				this.cdz != 0 ||
				this.cdy != 0 ||
				this.cdz != 0 ||
				this.clx != 0 ||
				this.cly != 0 ||
				this.clz != 0)) {
			double npw = ((PinholeCamera)camera).getProjectionWidth();
			if (this.pw != -1) npw = this.pw;
			
			double nph = ((PinholeCamera)camera).getProjectionHeight();
			if (this.ph != -1) nph = this.ph;
			
			double nfl = ((PinholeCamera)camera).getFocalLength();
			if (this.fl != -1) nfl = this.fl;
			
			net.sf.j3d.util.Vector cd = (net.sf.j3d.util.Vector)
								((PinholeCamera)camera).getViewDirection().clone();
			
			if (this.cdz != 0 ||
				this.cdy != 0 ||
				this.cdz != 0) {
				cd = new net.sf.j3d.util.Vector(this.cdx, this.cdy, this.cdz);
			}
			
			net.sf.j3d.util.Vector cl = (net.sf.j3d.util.Vector)
								((PinholeCamera)camera).getLocation().clone();
			
			if (this.clz != 0 ||
					this.cly != 0 ||
					this.clz != 0) {
					cl = new net.sf.j3d.util.Vector(this.clx, this.cly, this.clz);
				}
			
			camera = new PinholeCamera(cl, cd, new net.sf.j3d.util.Vector(0.0, 1.0, 0.0),
										nfl, npw, nph);
		}
		
		long start = System.currentTimeMillis();
		
		RenderParameters p = new RenderParameters(x, y, dx, dy, w, h, ssw, ssh);
		RGB rgb[][] = RayTracingEngine.render(s.getSurfaces(), camera, s.getLights(), p, null);
		
		long time = System.currentTimeMillis() - start;
		
		if (RayTracingJob.verboseRender)
			System.out.println("Done");
		
		String user = "", passwd = "";
		
		if (c != null) {
			user = c.getUser();
			passwd = c.getPassword();
		}
		
		RayTracingJobOutput jo = new RayTracingJobOutput(
									user, passwd,
									this.jobId + ":" +
									this.x + ":" + this.y + ":" +
									this.dx + ":" + this.dy);
		jo.setTime(time);
		
		for (int i = 0; i < rgb[0].length; i++) {
			for (int j = 0; j < rgb.length; j++) {
					jo.addRGB(rgb[j][i]);
			}
		}
		
		if (c == null || RayTracingJob.verboseRender) {
			File file = new File(this.jobId + "-" +
								this.x + "-" + this.y + "-" +
								this.w + "-" + this.h + "-" +
								this.ssw + "-" + this.ssh + ".jpg");
			try {
				FileEncoder.encodeImageFile(rgb, file, FileEncoder.JPEGEncoding);
			} catch (IOException e) {
				System.out.println("RayTracingJob: IO Error");
			}
		}
		
		if (c != null) c.writeOutput(jo);
	}
	

	/**
	 * @see net.sf.j3d.network.Job#getTaskId()
	 */
	public long getTaskId() { return this.jobId; }
	
	public String getTaskString() {
		return "RayTracingJobFactory: " + this.jobId + " 0.0 " +
			this.sceneUri + " " + this.w + "x" + this.h + " " +
			this.ssw + "x" + this.ssh + " " + this.pw + "x" + this.ph +
			" " + this.fl + " " + this.clx + "," + this.cly + "," + this.clz +
			" " + this.cdx + "," + this.cdy + "," + this.cdz;
	}
	
	public int hashcode() {
		return (int) ((this.jobId + this.x + this.y) % Integer.MAX_VALUE);
	}
	
	public boolean equals(Object o) {
		if (o instanceof RayTracingJob == false) return false;
		
		RayTracingJob j = (RayTracingJob) o;
		
		if (!this.sceneUri.equals(j.sceneUri)) return false;
		if (this.jobId != j.jobId) return false;
		if (this.x != j.x) return false;
		if (this.y != j.y) return false;
		if (this.dx != j.dx) return false;
		if (this.dy != j.dy) return false;
		if (this.w != j.w) return false;
		if (this.h != j.h) return false;
		if (this.ssw != j.ssw) return false;
		if (this.ssh != j.ssh) return false;
		
		return true;
	}
	
	public String toString() {
	    StringBuffer s = new StringBuffer();
	    
	    s.append(this.jobId);
	    s.append(" (");
	    s.append(this.x);
	    s.append(", ");
	    s.append(this.y);
	    s.append(") ");
	    s.append(this.dx);
	    s.append("x");
	    s.append(this.dy);
	    
	    return s.toString();
	}
}
