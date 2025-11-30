/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.photon.network;

import org.almostrealism.physics.AbsorberSet;
import org.almostrealism.raytrace.DefaultPhotonField;
import com.almostrealism.util.FileLoader;
import io.almostrealism.resource.Resource;
import io.flowtree.job.Job;
import io.flowtree.job.JobFactory;
import org.almostrealism.physics.Clock;
import org.almostrealism.primitives.AbsorptionPlane;
import org.almostrealism.util.KeyUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO  Since both JobFactory and Job now require a getCompletableFuture, this class will have to be separated
public class PhotonFieldJob implements JobFactory, Job {
	public static double verbose = Math.pow(10.0, -3.0);
	
	private final List running;
	
	private int tot;
	private String taskid;
	private long index;
	private double lifetime = Double.MAX_VALUE - 1.0;
	private String outDir = "/files/";
	private double pri = 1.0;
	
	private String file;
	private double tick;
	
	private boolean local;

	private final CompletableFuture<Void> future = new CompletableFuture<>();
	
	public PhotonFieldJob() {
		this.running = new ArrayList();
		this.taskid = KeyUtils.generateKey();
	}
	
	public PhotonFieldJob(String taskid, long index, String file, double tick, String out, double lifetime) {
		this.running = new ArrayList();
		
		this.taskid = taskid;
		this.index = index;
		this.file = file;
		this.tick = tick;
		this.outDir = out;
		this.lifetime = lifetime;
	}
	
	public String encode() {

		String b = this.getClass().getName() +
				":id=" +
				this.taskid +
				":file=" +
				this.file +
				":tick=" +
				this.tick +
				":index=" +
				this.index +
				":tot=" +
				this.tot +
				":out=" +
				this.outDir +
				":lifetime=" +
				this.lifetime;
		
		return b;
	}

	@Override
	public String getTaskId() { return this.taskid; }

	@Override
	public String getTaskString() { return "PhotonFieldTask (" + this.taskid + ")"; }

	@Override
	public void set(String key, String value) {
		if (key.equals("id"))
			this.taskid = value;
		else if (key.equals("file"))
			this.file = value;
		else if (key.equals("tick"))
			this.tick = Double.parseDouble(value);
		else if (key.equals("index"))
			this.index = Long.parseLong(value);
		else if (key.equals("tot"))
			this.tot = Integer.parseInt(value);
		else if (key.equals("out"))
			this.outDir = value;
		else if (key.equals("lifetime"))
			this.lifetime = Double.parseDouble(value);
	}
	
	public void run() {
		Resource r = null; // Client.getCurrentClient().getServer().loadResource(this.file, this.local); TODO
		
		AbsorberSet a = null;
		
		try {
			if (r == null) {
				System.out.println("PhotonFieldJob: Could not load " + this.file);
				throw new IllegalArgumentException("Could not load " + this.file);
			}
			
			a = FileLoader.loadSet(r.getInputStream());
		} catch (SAXException e) {
			System.out.println("PhotonFieldJob: Could not load absorber set (" +
								e.getMessage() + ")");
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.out.println("PhotonFieldJob: Could not load absorber set (" +
								e.getMessage() + ")");
			throw new RuntimeException(e);
		}
		
		AbsorptionPlane plane = null;
		
		Iterator itr = a.absorberIterator();
		
		w: while (itr.hasNext()) {
			Object o = itr.next();
			
			if (o instanceof AbsorptionPlane) {
				plane = (AbsorptionPlane) o;
				break;
			}
		}
		
		DefaultPhotonField f = new DefaultPhotonField();
		f.setMaxLifetime(this.lifetime);
		f.setAbsorber(a);
		
		Clock c = new Clock();
		c.setTickInterval(this.tick);
		
		System.out.println("PhotonFieldJob: Tick = " + this.tick);
		
		c.addPhotonField(f);
		a.setClock(c);

		/* TODO
		Client.getCurrentClient().getServer().
							addLogItem("Photon Count for " + this.getTaskString(),
										f.getSizeChart());
		Client.getCurrentClient().getServer().
							addLogItem("Hours per Microsecond for " + this.getTaskString(),
										f.getTimeChart());
		Client.getCurrentClient().getServer().
				addLogItem("Cost of one microsecond on a t2.medium for " + this.getTaskString(),
										f.getCostChart());
										*/
		
		long start = System.currentTimeMillis();
		
		boolean first = true;
		
		w: while (true) {
			c.tick().get().run();
			
			if (first || c.getTicks() % 100000 == 0) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				long num = f.getSize();
				
				System.out.println("PhotonFieldJob[" + c.getTime() + "]: " + rate +
									" hours per microsecond. " + num + " photons.");
				
				if (plane == null) {
					System.out.println("PhotonFieldJob: No absorption plane.");
					continue w;
				}
				
				if (plane.imageAvailable()) {
					String outputfile = this.outDir +
										"PhotonFieldTask-" + this.taskid +
											"-" + this.index + ".ppm";

					/* TODO
					try (OutputStream o = Client.getCurrentClient().
										getServer().getOutputStream(outputfile)) {
						plane.writeImage(o);
						System.out.println("PhotonFieldJob: Wrote " + outputfile);
					} catch (IOException ioe) {
						System.out.println("PhotonFieldJob: Could not write image (" +
											ioe.getMessage() + ")");
					}
					*/
				} else {
					System.out.println("PhotonFieldJob: No image available.");
				}
				
				first = false;
			}
		}
	}
	
	@Override
	public Job nextJob() {
		/* TODO
		Server s = Client.getCurrentClient().getServer();
		String f = null;
		
		if (s.isDirectory(this.file) && this.running.size() < this.tot) {
			String files[] = s.getChildren(this.file);
			
			i: for (int i = 0; i < files.length; i++) {
				if (files[i].equals(this.file)) continue i;
				if ((files[i] + "/").equals(this.file)) continue i;
				if (!this.running.contains(files[i])) {
					f = files[i];
					break i;
				}
			}
			
			if (f != null) {
				s.addTask(new PhotonFieldJob(System.currentTimeMillis(), 0, f,
											this.tick, this.outDir, this.lifetime));
				this.running.add(f);
			}
			
			return null;
		}
		
		if (this.index >= this.tot) return null;
		*/
		
		return new PhotonFieldJob(this.taskid, this.index++,
									this.file, this.tick, this.outDir, this.lifetime);
	}
	
	public Job createJob(String data) {
//		return Server.instantiateJobClass(data); TODO
		return null;
	}

	public double getCompleteness() { return 0.0; }
	public String getName() { return "PhotonFieldTask"; }
	public void setPriority(double p) { this.pri = p; }
	public double getPriority() { return this.pri; }

	@Override
	public CompletableFuture<Void> getCompletableFuture() { return future; }

	public boolean isComplete() { return this.index >= this.tot; }
	
	public String toString() { return "PhotonFieldTask (" + this.taskid + ")"; }
}
