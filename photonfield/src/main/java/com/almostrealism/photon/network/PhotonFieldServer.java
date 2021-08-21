package com.almostrealism.photon.network;

import com.almostrealism.physics.BlackBody;
import io.flowtree.cli.FlowTreeCliServer;
import io.flowtree.fs.OutputServer;
import io.flowtree.job.Job;
import io.flowtree.job.JobFactory;
import io.flowtree.jobs.TemporalJob;
import org.almostrealism.time.Temporal;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;

public class PhotonFieldServer {
	public static void main(String args[]) throws FileNotFoundException {
		FlowTreeCliServer.start(args);

		final String sceneFile = BlackBody.createScene();

		OutputServer.getCurrentServer().getNodeServer().addTask(new JobFactory() {
			@Override
			public String getTaskId() {
				return "";
			}

			@Override
			public Job nextJob() {
				Temporal c;

				try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(sceneFile))) {
					c = (Temporal) decoder.readObject();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				}

				return new TemporalJob(c, 10) {
					public void run() {
						super.run();
						System.out.println("Completed iterations");
						System.out.println("Writing " + sceneFile);

						try {
							XMLEncoder encoder = new XMLEncoder(new FileOutputStream(sceneFile));
							encoder.writeObject(getTemporal());
							System.out.println("Done writing xml");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				};
			}

			@Override
			public Job createJob(String s) {
				return null;
			}

			@Override
			public void set(String s, String s1) {

			}

			@Override
			public String encode() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public double getCompleteness() {
				return 0;
			}

			@Override
			public boolean isComplete() {
				return false;
			}

			@Override
			public void setPriority(double v) {

			}

			@Override
			public double getPriority() {
				return 1.0;
			}

			@Override
			public CompletableFuture<Void> getCompletableFuture() {
				return new CompletableFuture<>();
			}
		});
	}
}
