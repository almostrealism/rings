package com.almostrealism.network;

import io.almostrealism.db.Query;
import io.almostrealism.db.QueryHandler;
import org.almostrealism.color.RGB;
import org.almostrealism.io.JobOutput;
import org.almostrealism.io.OutputHandler;
import org.almostrealism.texture.ImageCanvas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// TODO  This class behaves in one way when it is initialied
//  	 without a task ID, and a different way when it has
//		 one. These should be separated into two implementations.

public class RayTracingOutputHandler implements OutputHandler, QueryHandler {
	private static List completedTasks;

	private RGB image[][];
	private String taskId;

	private Set children;
	private String lastTaskId, currentTaskId;
	private boolean recievedQuery;

	public RayTracingOutputHandler() { this(null, 0, 0, false); }

	public RayTracingOutputHandler(String id) { this(id, 0, 0, false); }

	public RayTracingOutputHandler(String id, int w, int h) { this(id, w, h, false); }

	public RayTracingOutputHandler(String id, int w, int h, boolean startImageThread) {
		System.out.println("Constructing RayTracingOutputHandler " + this.hashCode() + ": " + id + " " + w + " " + h);

		if (RayTracingOutputHandler.completedTasks == null)
			RayTracingOutputHandler.completedTasks = new ArrayList();

		this.taskId = id;

		if (this.taskId == null) {
			this.children = new HashSet();
			RayTracingJob.defaultOutputHandler = this;
		} else {
			this.image = new RGB[w][h];

			if (startImageThread) {
				Thread t = new Thread(new Runnable() {
					public void run() {
						if (RayTracingOutputHandler.this.taskId == null) return;

						System.out.println("RayTracingJobOutputHandler: Started file output thread for " +
								RayTracingOutputHandler.this.taskId + ".");

						w:
						while (!RayTracingOutputHandler.this.isComplete()) {
							try {
								Thread.sleep(1200000);

								if (RayTracingOutputHandler.this.image.length <= 0) continue w;

								System.out.println("RayTracingJobOutputHandler: Writing image for task " +
										RayTracingOutputHandler.this.taskId + " (" +
										RayTracingOutputHandler.this.image.length + ", " +
										RayTracingOutputHandler.this.image[0].length + ")...");

								try (PrintStream p = new PrintStream(new FileOutputStream(
										"images/NetworkRender-" +
												RayTracingOutputHandler.this.taskId + ".raw"))) {
									for (int i = 0; i < RayTracingOutputHandler.this.image.length; i++) {
										for (int j = 0; j < RayTracingOutputHandler.this.image[i].length; j++) {
											p.println("[" + i + ", " + j + "]: " +
													RayTracingOutputHandler.this.image[i][j]);
										}
									}
								}

								ImageCanvas.encodeImageFile(RayTracingOutputHandler.this.getImage(),
										new File("images/NetworkRender-" + RayTracingOutputHandler.this.taskId + ".jpg"),
										ImageCanvas.JPEGEncoding);
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
	}

	public void writeImage() {
		if (RayTracingOutputHandler.this.image.length <= 0) return;

		System.out.println("RayTracingOutputHandler: Writing image for task " +
				RayTracingOutputHandler.this.taskId + " (" +
				RayTracingOutputHandler.this.image.length + ", " +
				RayTracingOutputHandler.this.image[0].length + ")...");

		try (PrintStream p = new PrintStream(new FileOutputStream(
				"images/NetworkRender-" + RayTracingOutputHandler.this.taskId + ".raw"))) {
			for (int i = 0; i < RayTracingOutputHandler.this.image.length; i++) {
				for (int j = 0; j < RayTracingOutputHandler.this.image[i].length; j++) {
					p.println("[" + i + ", " + j + "]: " + RayTracingOutputHandler.this.image[i][j]);
				}
			}

			ImageCanvas.encodeImageFile(RayTracingOutputHandler.this.getImage(),
						new File("images/NetworkRender-" + RayTracingOutputHandler.this.taskId + ".jpg"),
						ImageCanvas.JPEGEncoding);
		} catch (IOException ioe) {
			System.out.println("RayTracingJobOutputHandler: " + ioe);
		}
	}

	public RayTracingOutputHandler getHandler(String task) {
		if (task.equals(this.taskId)) return this;
		if (this.children == null) return null;

		Iterator itr = this.children.iterator();
		while (itr.hasNext()) {
			RayTracingOutputHandler h = ((RayTracingOutputHandler)itr.next()).getHandler(task);
			if (h != null) return h;
		}

		return null;
	}

	public String getId() { return this.taskId; }

	public boolean isComplete() {
		if (this.taskId == null || !this.recievedQuery) return false;

		for (int i = 0; i < this.image.length; i++) {
			for (int j = 0; j < this.image[i].length; j++) {
				if (this.image[i][j] == null) return false;
			}
		}

		return true;
	}

	@Override
	public void storeOutput(long time, int uid, JobOutput data) {
		if (data instanceof RayTracingJobOutput == false) {
			System.out.println("RayTracingOutputHandler (" + this.taskId + ") received: " + data);
			return;
		}

		RayTracingJobOutput output = (RayTracingJobOutput) data;

		if (output.getTaskId() == null) {
			System.out.println("RayTracingOutputHandler (" + this.taskId + ") received: " + data);
			return;
		}

		t: if (this.taskId == null) {
			Iterator itr = this.children.iterator();

			String id = output.getTaskId();

			while (itr.hasNext()) {
				RayTracingOutputHandler h = (RayTracingOutputHandler) itr.next();

				if (h.isComplete()) itr.remove();

				if (h.getId() == id) {
					h.storeOutput(time, uid, data);
					break t;
				}
			}

			System.out.println("RayTracingOutputHandler: Received " + output);
			System.out.println("RayTracingOutputHandler: Spawning Output Handler for job " + id + "...");

			RayTracingOutputHandler h = new RayTracingOutputHandler(id, output.getDx(), output.getDy());
			h.storeOutput(time, uid, data);

			this.children.add(h);

			this.lastTaskId = this.currentTaskId;
			this.currentTaskId = id;

			System.out.println("RayTracingJob: Writing index.html");

			String s = RayTracingJob.htmlPre + this.lastTaskId + RayTracingJob.htmlPost;

			try (PrintStream out = new PrintStream(new FileOutputStream("index.html"))) {
				out.println(s);
			} catch (IOException ioe) {
				System.out.println("RayTracingOutputHandler: IO error writing index.html (" +
						ioe.getMessage() + ")");
			}
		}

		this.addToImage(output, output.getX(), output.getY(), output.getDx(), output.getDy());
	}

	@Override
	public Hashtable executeQuery(Query q) {
		if (this.taskId == null) {
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

			System.out.println("RayTracingJobOutputHandler: Received query for " +
					q.getTable() + " (" + q.getCondition() + ")");
		}

		if (!q.getTable().equals("image-" + this.taskId)) return null;

		System.out.println("RayTracingJobOutputHandler: Received query for " +
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

			System.out.println("RayTracingOutputHandler (" + this.taskId + ") expanded image buffer to: " + w + " " + h);
		}
	}

	protected synchronized void addToImage(RayTracingJobOutput data, int x, int y, int dx, int dy) {
		if (this.taskId == null) return;
		if (this.image == null) this.image = new RGB[0][0];

		int w = this.image.length, h;

		if (w <= 0)
			h = 0;
		else
			h = this.image[0].length;

		if (x + dx > w) w = x + dx;
		if (y + dy > h) h = y + dy;

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
