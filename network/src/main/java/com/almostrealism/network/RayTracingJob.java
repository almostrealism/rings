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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.almostrealism.io.SceneLoader;
import com.almostrealism.raytrace.FogParameters;
import com.almostrealism.raytrace.RayIntersectionEngine;
import com.almostrealism.raytrace.RenderParameters;
import com.almostrealism.raytracer.RayTracedScene;
import io.flowtree.job.Output;
import org.almostrealism.geometry.Camera;
import org.almostrealism.color.RGB;
import org.almostrealism.space.FileDecoder;
import org.almostrealism.io.JobOutput;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

import com.almostrealism.io.FilePrintWriter;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.raytracer.Settings;

import io.flowtree.job.Job;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.util.CodeFeatures;

/**
 * A {@link RayTracingJob} provides an implementation of {@link Job}
 * that renders a section of an image.
 * 
 * @author  Michael Murray
 */
public class RayTracingJob implements Job, CodeFeatures {
	public static final String htmlPre = "<html> <head> <title>Universe in a Box</title> </head> " +
										"<body bgcolor=\"#000000\" text=\"#ffffff\"> <center> " +
										"<h1>Universe in a Box</h1> <img src=\"images/NetworkRender-";
	public static final String htmlPost = ".jpg\"/> </center> </body> </html>";
	
	public static boolean verboseRender = false;

	public boolean local = false;
	
	protected static RayTracingOutputHandler defaultOutputHandler;

	private static Map scenes;
	private static List loading;

	private String sceneUri, sLoader;
	private int x, y, dx, dy, w, h, ssw, ssh;

	private String outputHost;
	private int outputPort = 7788;

	private String jobId;

	private double pw = -1.0, ph = -1.0;
	private double fl = -1.0;
	private double clx, cly, clz;
	private double cdx, cdy, cdz;

	private ExecutorService pool;
	private CompletableFuture<Void> future = new CompletableFuture<>();
	private Consumer<JobOutput> outputConsumer;

	/**
	 * Constructs a new {@link RayTracingJob}.
	 */
	public RayTracingJob() {
	    if (RayTracingJob.scenes == null)
	    		RayTracingJob.scenes = Collections.synchronizedMap(new Hashtable());
	    
	    if (RayTracingJob.loading == null)
	    		RayTracingJob.loading = Collections.synchronizedList(new ArrayList());
	    
	}
	
	/**
	 * Constructs a new {@link RayTracingJob}.
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
						int ssw, int ssh, String jobId) {
		if (RayTracingJob.scenes == null) RayTracingJob.scenes = new Hashtable();
		if (RayTracingJob.loading == null) RayTracingJob.loading = new ArrayList();

		if (dx == 0 || dy == 0) {
			throw new IllegalArgumentException("Invalid dx/dy");
		}

		if (x + dx > w || y + dy > h) {
			throw new IllegalArgumentException("Invalid position in image");
		}
		
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

		if (x + dx > image.length || y + dy > image[x].length) {
			throw new IllegalArgumentException(x + "," + y + ":" + dx + "x" + dy + " is not contained in the image");
		}
		
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
	 * @return  The scene referenced by this {@link RayTracingJob}.
	 */
	public Scene<ShadableSurface> getScene() {
		Scene<ShadableSurface> s = null;
		
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

					SceneLoader loader = (uri) -> {
						try {
							return FileDecoder.decodeScene(new URL(uri).openStream(), FileDecoder.XMLEncoding,
									false, Exception::printStackTrace);
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					};

					if (this.sLoader != null) {
						Object l = Class.forName(this.sLoader).getConstructor().newInstance();

						if (l instanceof SceneLoader)
							loader = (SceneLoader) l;
						else
							System.out.println("RayTracingJob: " + this.sLoader +
									" is not a valid SceneLoader.");
					}

					System.out.println("RayTracingJob: Loading scene from " +
							this.sceneUri + " via " + loader);

					s = loader.apply(this.sceneUri);
					if (s == null) throw new IOException();

					System.out.println("RayTracingJob: Scene loaded.");

					RayTracingJob.scenes.put(this.sceneUri, s);
				} catch (NoSuchMethodException | InvocationTargetException m) {
					System.out.println("RayTracingJob: Error creating SceneLoader - " + m);
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

	@Override
	public void setOutputConsumer(Consumer<JobOutput> outputConsumer) {
		this.outputConsumer = outputConsumer;
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

	public void setOutputHost(String host) { this.outputHost = host; }

	public String getOutputHost() { return outputHost; }

	public void setOutputPort(int port) { this.outputPort = port; }

	public int getOutputPort() { return this.outputPort; }
	
	/**
	 * @see io.flowtree.job.Job#encode()
	 */
	@Override
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

		if (this.outputHost != null) {
			s.append(":oh=");
			s.append(outputHost);
		}

		s.append(":op=");
		s.append(outputPort);
		
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
	 * @see io.flowtree.job.Job#set(java.lang.String, java.lang.String)
	 */
	@Override
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
			this.jobId = value;
		else if (key.equals("oh"))
			this.outputHost = value;
		else if (key.equals("op"))
			this.outputPort = Integer.parseInt(value);
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
	@Override
	public void run() {
		Scene<ShadableSurface> s = this.getScene();
		
		if (RayTracingJob.verboseRender)
			System.out.println("Got scene: " + s);
		
		if (s == null) {
			System.out.println("RayTracingJob: No scene data available.");
			future.completeExceptionally(new RuntimeException("No scene data available"));
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
			
			org.almostrealism.algebra.Vector cd = (org.almostrealism.algebra.Vector)
								((PinholeCamera)camera).getViewDirection().clone();
			
			if (this.cdz != 0 ||
				this.cdy != 0 ||
				this.cdz != 0) {
				cd = new org.almostrealism.algebra.Vector(this.cdx, this.cdy, this.cdz);
			}
			
			org.almostrealism.algebra.Vector cl = (org.almostrealism.algebra.Vector)
								((PinholeCamera)camera).getLocation().clone();
			
			if (this.clz != 0 ||
					this.cly != 0 ||
					this.clz != 0) {
					cl = new org.almostrealism.algebra.Vector(this.clx, this.cly, this.clz);
				}
			
			camera = new PinholeCamera(cl, cd, new org.almostrealism.algebra.Vector(0.0, 1.0, 0.0),
										nfl, npw, nph);
		}
		
		long start = System.currentTimeMillis();
		
		RenderParameters p = new RenderParameters(x, y, dx, dy, w, h, ssw, ssh);
		RayTracedScene r = new RayTracedScene(new RayIntersectionEngine((Scene<ShadableSurface>) s,
												new FogParameters()), s.getCamera(), p, getExecutorService());
		Producer<RGB[][]> renderedImageData = r.realize(p);

		RGB rgb[][] = renderedImageData.get().evaluate(x, y);
		
		long time = System.currentTimeMillis() - start;
		
		if (RayTracingJob.verboseRender)
			System.out.println("Done");
		
		String user = "", passwd = "";

//		TODO
//		if (c != null) {
//			user = c.getUser();
//			passwd = c.getPassword();
//		}

		if (this.x + dx > this.w || this.y + dy > h) {
			System.out.println("WARN: Image bounds exceeded");
		}
		
		RayTracingJobOutput jo = new RayTracingJobOutput(
									this.jobId,
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

		// System.out.println("RayTracingJob: There are " + jo.size() + " RGBs for " + toString());

		if (RayTracingJob.verboseRender) {
			File file = new File(this.jobId + "-" +
								this.x + "-" + this.y + "-" +
								this.w + "-" + this.h + "-" +
								this.ssw + "-" + this.ssh + ".jpg");

			try {
				ImageCanvas.encodeImageFile(v(rgb).get(), file, ImageCanvas.JPEGEncoding);
			} catch (IOException e) {
				System.out.println("RayTracingJob: IO Error");
			}
		}

		if (outputHost != null) {
			new Output(outputHost, outputPort).apply(jo);
		} else if (outputConsumer != null) {
			outputConsumer.accept(jo);
		}

		future.complete(null);
	}
	

	/**
	 * @see io.flowtree.job.Job#getTaskId()
	 */
	@Override
	public String getTaskId() { return this.jobId; }

	@Override
	public String getTaskString() {
		return "RayTracingJobFactory: " + this.jobId + " 0.0 " +
			this.sceneUri + " " + this.w + "x" + this.h + " " +
			this.ssw + "x" + this.ssh + " " + this.pw + "x" + this.ph +
			" " + this.fl + " " + this.clx + "," + this.cly + "," + this.clz +
			" " + this.cdx + "," + this.cdy + "," + this.cdz;
	}

	/**
	 * Provide the {@link ExecutorService} to be used by {@link com.almostrealism.raytracer.RayTracer}.
	 */
	@Override
	public void setExecutorService(ExecutorService pool) {
		this.pool = pool;
	}

	/**
	 * Return the {@link ExecutorService} to be used by {@link com.almostrealism.raytracer.RayTracer}.
	 */
	public ExecutorService getExecutorService() {
		return this.pool;
	}

	@Override
	public CompletableFuture<Void> getCompletableFuture() { return future; }

	@Override
	public int hashCode() { return jobId.hashCode(); }

	@Override
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

	    s.append("[task ");
	    s.append(this.jobId);
	    s.append(" (");
	    s.append(this.x);
	    s.append(", ");
	    s.append(this.y);
	    s.append(") ");
	    s.append(this.dx);
	    s.append("x");
	    s.append(this.dy);
	    s.append("]");
	    
	    return s.toString();
	}
}
