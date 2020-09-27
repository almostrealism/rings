/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.raytracer;

import com.almostrealism.LegacyRayTracingEngine;
import com.almostrealism.lighting.DirectionalAmbientLight;
import com.almostrealism.lighting.SphericalLight;
import com.almostrealism.lighting.StandardLightingRigs;
import com.almostrealism.primitives.RigidPlane;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.projection.ThinLensCamera;
import com.almostrealism.rayshade.BlendingShader;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.RigidBodyStateShader;
import com.almostrealism.primitives.RigidSphere;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shader;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.physics.RigidBody;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.swing.JTextAreaPrintWriter;
import org.almostrealism.texture.GraphicsConverter;
import org.almostrealism.time.Animation;
import org.almostrealism.util.StaticProducer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class RayTracedAnimation<T extends ShadableSurface> extends Animation<T> {
	private int imageWidth, imageHeight;
	private Image image;

	private List<File> inputFiles;

	public RayTracedAnimation() {
		this.inputFiles = new ArrayList<>();
	}

	public void setImageDimensions(int w, int h) {
		this.imageWidth = w;
		this.imageHeight = h;
	}

	/**
	 * @return  An AWT Image object storing the most recent image data for this Simulation object.
	 */
	public Image getImage() { return this.image; }

	/**
	 * Writes the current image to a file that is labeled using the specified values.
	 *
	 * @param i  The iteration number of the image.
	 * @param instance  The instance string for the animation.
	 */
	public void writeImage(int i, String instance) {
		try {
			System.out.print("Encoding frame " + i + "/" + this.getIterations() + ": ");

			String fn = this.getOutputDirectory() + "/frame_" + instance + "." + i + ".jpeg";
			File f = new File(fn);

			RGB image[][] = LegacyRayTracingEngine.render(this, this.imageWidth, this.imageHeight, 1, 1, null);
			this.image = GraphicsConverter.convertToAWTImage(image);

			BufferedImage buff = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = buff.getGraphics();
			g.drawImage(this.image, 0, 0, null);
			g.setColor(Color.black);
			g.setFont(new Font("Monospaced", Font.PLAIN, 16));
			// g.drawString(this.bodies[0].toString(), 10, this.imageHeight - 30);

//			TODO  Write image
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(f));
//			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(buff);
//			param.setQuality(1.0f, true);
//			encoder.encode(buff, param);

			this.inputFiles.add(f);
			System.out.println(fn);
		} catch (Exception ioe) {
			System.err.println("Error writing image file for frame " + i + " : " + ioe.toString());
		}
	}

	/**
	 * Write a script to be used to compose the output image to form an animation.
	 *
	 * @param instance  The instance string for the animation.
	 */
	// TODO  Respect instance so generated files do not collide
	public void writeEncodeScript(String instance) {
		System.out.print("Writing encode script: ");

		try (PrintWriter out = new PrintWriter(new FileWriter(new File(getOutputDirectory()+ "encode.sh")))) {
			out.println("#!/bin/sh");
			out.print("mencoder mf://");

			Iterator itr = this.inputFiles.iterator();
			int i = 0;
			int l = this.inputFiles.size();

			while (itr.hasNext()) {
				out.print(((File)itr.next()).getName());
				if (i < l) out.print(",");

				i++;
			}

			int w = this.imageWidth;
			int h = this.imageHeight;
			int fps = (int)(1 / this.getFrameDuration());

			out.print(" -mf w=" + w + ":h=" + h + ":fps=" + fps + ":type=jpg -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o output.avi");
			out.println();
			out.flush();
			out.close();

			System.out.println("Done");
		} catch (IOException ioe) {
			System.out.println("Error writing encode script: " + ioe);
		}
	}

	/**
	 * @return  A Properties object containing all of the data required to reconstruct the current state
	 *          of the RigidBody objects stored by this Simulation object.
	 */
	public Properties generateProperties() {
		Properties p = super.generateProperties();

		p.setProperty("bodies.length", String.valueOf(size()));

		if (LegacyRayTracingEngine.castShadows == false) p.setProperty("render.shadows", "false");

		Vector cl = ((PinholeCamera) getCamera()).getLocation();
		Vector cv = ((PinholeCamera) getCamera()).getViewingDirection();

		double foc = ((PinholeCamera) getCamera()).getFocalLength();
		double w = ((PinholeCamera) getCamera()).getProjectionWidth();
		double h = ((PinholeCamera) getCamera()).getProjectionHeight();

		p.setProperty("camera.loc.x", String.valueOf(cl.getX()));
		p.setProperty("camera.loc.y", String.valueOf(cl.getY()));
		p.setProperty("camera.loc.z", String.valueOf(cl.getZ()));

		p.setProperty("camera.view.x", String.valueOf(cv.getX()));
		p.setProperty("camera.view.y", String.valueOf(cv.getY()));
		p.setProperty("camera.view.z", String.valueOf(cv.getZ()));

		p.setProperty("camera.foc", String.valueOf(foc));
		p.setProperty("camera.proj.w", String.valueOf(w));
		p.setProperty("camera.proj.h", String.valueOf(h));

		i: for (int i = 0; i < size(); i++) {
			AbstractSurface surface = null;
			if (super.get(i) instanceof AbstractSurface) surface = (AbstractSurface) super.get(i);

			if (surface instanceof RigidSphere) {
				RigidSphere s = (RigidSphere) surface;

				p.setProperty("bodies." + i + ".type", "sphere");
				p.setProperty("bodies." + i + ".size", String.valueOf(s.getSize()));

				SphericalLight light = s.getLight();

				if (light != null) {
					double at[] = light.getAttenuationCoefficients();

					p.setProperty("bodies." + i + ".light.on", "true");
					p.setProperty("bodies." + i + ".light.intensity", String.valueOf(light.getIntensity()));
					p.setProperty("bodies." + i + ".light.samples", String.valueOf(light.getSampleCount()));
					p.setProperty("bodies." + i + ".light.ata", String.valueOf(at[0]));
					p.setProperty("bodies." + i + ".light.atb", String.valueOf(at[1]));
					p.setProperty("bodies." + i + ".light.atc", String.valueOf(at[2]));
				}
			} else if (surface instanceof RigidPlane) {
				RigidPlane s = (RigidPlane) surface;

				p.setProperty("bodies." + i + ".type", "plane");
				p.setProperty("bodies." + i + ".size", String.valueOf(s.getSize()));
			}

			if (surface != null) {
				Iterator itr = surface.getShaderSet().iterator();

				w: while (itr.hasNext()) {
					Shader sh = (Shader)itr.next();

					if (sh instanceof RigidBodyStateShader) {
						int type = ((RigidBodyStateShader)sh).getType();
						String rshtype = "";

						if (type == RigidBodyStateShader.FORCE)
							rshtype = "force";
						else if (type == RigidBodyStateShader.VELOCITY)
							rshtype = "velocity";
						else
							continue w;

						p.setProperty("bodies." + i + ".shade.rbstate", rshtype);
					} else if (sh instanceof ReflectionShader) {
						p.setProperty("bodies." + i + ".shade.ref",
								String.valueOf(((ReflectionShader) sh).getReflectivity()));
					}
				}
			}

			if (get(i) instanceof RigidBody == false) continue i;

			p.setProperty("bodies." + i + ".mass", String.valueOf(((RigidBody) get(i)).getState().getMass()));

			Vector loc = ((RigidBody) get(i)).getState().getLocation();
			p.setProperty("bodies." + i + ".loc.x", String.valueOf(loc.getX()));
			p.setProperty("bodies." + i + ".loc.y", String.valueOf(loc.getY()));
			p.setProperty("bodies." + i + ".loc.z", String.valueOf(loc.getZ()));

			Vector rot = ((RigidBody) get(i)).getState().getRotation();
			p.setProperty("bodies." + i + ".rot.x", String.valueOf(rot.getX()));
			p.setProperty("bodies." + i + ".rot.y", String.valueOf(rot.getY()));
			p.setProperty("bodies." + i + ".rot.z", String.valueOf(rot.getZ()));

			Vector lv = ((RigidBody) get(i)).getState().getLinearVelocity();
			p.setProperty("bodies." + i + ".lv.x", String.valueOf(lv.getX()));
			p.setProperty("bodies." + i + ".lv.y", String.valueOf(lv.getY()));
			p.setProperty("bodies." + i + ".lv.z", String.valueOf(lv.getZ()));

			Vector av = ((RigidBody) get(i)).getState().getAngularVelocity();
			p.setProperty("bodies." + i + ".av.x", String.valueOf(av.getX()));
			p.setProperty("bodies." + i + ".av.y", String.valueOf(av.getY()));
			p.setProperty("bodies." + i + ".av.z", String.valueOf(av.getZ()));
		}

		return p;
	}

	@Override
	public void loadProperties(Properties p) {
		super.loadProperties(p);

		ThinLensCamera c;

		String d = p.getProperty("render.debug");
		if (d != null && d.equals("true")) {
			Settings.produceOutput = true;
			Settings.produceRayTracingEngineOutput = true;
			Settings.rayEngineOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			Settings.produceShaderOutput = true;
			Settings.shaderOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			DiffuseShader.produceOutput = true;

			DebugOutputPanel outputPanel = new DebugOutputPanel();
			outputPanel.showPanel();
		}

		String shadows = p.getProperty("render.shadows");
		if (shadows != null && shadows.equals("false")) LegacyRayTracingEngine.castShadows = false;

		List bodies = new ArrayList();

		Vector cl = new Vector(0.0, 0.0, 0.0);
		Vector cv = new Vector(0.0, 0.0, 1.0);

		String cx = p.getProperty("camera.loc.x");
		String cy = p.getProperty("camera.loc.y");
		String cz = p.getProperty("camera.loc.z");

		String vx = p.getProperty("camera.view.x");
		String vy = p.getProperty("camera.view.y");
		String vz = p.getProperty("camera.view.z");

		String f = p.getProperty("camera.foc");
		String fl = p.getProperty("camera.foclen");
		String lr = p.getProperty("camera.lensradius");
		String pw = p.getProperty("camera.proj.w");
		String ph = p.getProperty("camera.proj.h");

		if (cx != null) cl.setX(Double.parseDouble(cx));
		if (cy != null) cl.setY(Double.parseDouble(cy));
		if (cz != null) cl.setZ(Double.parseDouble(cz));

		if (vx != null) cv.setX(Double.parseDouble(vx));
		if (vy != null) cv.setY(Double.parseDouble(vy));
		if (vz != null) cv.setZ(Double.parseDouble(vz));

		c = new ThinLensCamera();
		c.setLocation(cl);
		c.setViewingDirection(cv);
		c.setUpDirection(new Vector(0.0, 1.0, 0.0));

		if (f != null) c.setFocus(Double.parseDouble(f));
		if (fl != null) c.setFocalLength(Double.parseDouble(fl));
		if (lr != null) c.setLensRadius(Double.parseDouble(lr));
		if (pw != null) c.setProjectionWidth(Double.parseDouble(pw));
		if (ph != null) c.setProjectionHeight(Double.parseDouble(ph));

		c.updateUVW();

		int len = Integer.parseInt(p.getProperty("bodies.length", "0"));

		i: for (int i = 0; i < len; i++) {
			String type = p.getProperty("bodies." + i + ".type");
			double size = Double.parseDouble(p.getProperty("bodies." + i + ".size", "1.0"));

			RigidBody b = null;

			if (type == null) {
				continue i;
			} else if (type.equals("sphere")) {
				b = new RigidSphere();
				((RigidSphere)b).setRadius(size);
				((RigidSphere)b).setColor(new RGB(0.8, 0.8, 0.8));

				String lit = p.getProperty("bodies." + i + ".light.on");

				if (lit != null) {
					double intensity = Double.parseDouble(p.getProperty("bodies." + i + ".light.intensity", "0.0"));
					int samples = Integer.parseInt(p.getProperty("bodies." + i + ".light.samples", "0"));

					double ata = Double.parseDouble(p.getProperty("bodies." + i + ".light.ata", "0.0"));
					double atb = Double.parseDouble(p.getProperty("bodies." + i + ".light.atb", "0.0"));
					double atc = Double.parseDouble(p.getProperty("bodies." + i + ".light.atc", "1.0"));

					((RigidSphere)b).setLighting(true);

					SphericalLight light = ((RigidSphere)b).getLight();
					light.setColor(new RGB(1.0, 1.0, 1.0));
					light.setIntensity(intensity);
					light.setSampleCount(samples);
					light.setAttenuationCoefficients(ata, atb, atc);

					super.addLight((Light)b);
				}
			} else if (type.equals("plane")) {
				b = new RigidPlane();
				((RigidPlane)b).setColor(new RGB(0.8, 0.8, 0.8));

				String lit = p.getProperty("bodies." + i + ".light.on");

				if (lit != null) {
					double intensity = Double.parseDouble(p.getProperty("bodies." + i + ".light.intensity", "0.0"));
					int samples = Integer.parseInt(p.getProperty("bodies." + i + ".light.samples", "0"));

					double ata = Double.parseDouble(p.getProperty("bodies." + i + ".light.ata", "0.0"));
					double atb = Double.parseDouble(p.getProperty("bodies." + i + ".light.atb", "0.0"));
					double atc = Double.parseDouble(p.getProperty("bodies." + i + ".light.atc", "1.0"));

					((RigidSphere)b).setLighting(true);

					SphericalLight light = ((RigidSphere)b).getLight();
					light.setColor(new RGB(1.0, 1.0, 1.0));
					light.setIntensity(intensity);
					light.setSampleCount(samples);
					light.setAttenuationCoefficients(ata, atb, atc);

					super.addLight((Light)b);
				}
			}

			if (b instanceof AbstractSurface) {
				AbstractSurface s = (AbstractSurface)b;

				String rbstate = p.getProperty("bodies." + i + ".shade.rbstate");

				if (rbstate != null) {
					BlendingShader bs = new BlendingShader(StaticProducer.of(new RGB(0.8, 0.0, 0.0)),
															StaticProducer.of(new RGB(0.0, 0.0, 0.8)));

					if (rbstate.equals("force"))
						s.addShader(new RigidBodyStateShader(RigidBodyStateShader.FORCE, 0.0, 1.0, bs));
					else if (rbstate.equals("velocity"))
						s.addShader(new RigidBodyStateShader(RigidBodyStateShader.VELOCITY, 0.0, 1.0, bs));
				}

				String ref = p.getProperty("bodies." + i + ".shade.ref");

				if (ref != null) {
					s.addShader(new ReflectionShader(Double.parseDouble(ref), RGBWhite.getInstance()));
				}

				s.addShader(DiffuseShader.defaultDiffuseShader);
			}

			RigidBody.State s = b.getState();

			double mass = Double.parseDouble(p.getProperty("bodies." + i + ".mass", "1.0"));

			double locX = Double.parseDouble(p.getProperty("bodies." + i + ".loc.x", "0.0"));
			double locY = Double.parseDouble(p.getProperty("bodies." + i + ".loc.y", "0.0"));
			double locZ = Double.parseDouble(p.getProperty("bodies." + i + ".loc.z", "0.0"));

			double rotX = Double.parseDouble(p.getProperty("bodies." + i + ".rot.x", "0.0"));
			double rotY = Double.parseDouble(p.getProperty("bodies." + i + ".rot.y", "0.0"));
			double rotZ = Double.parseDouble(p.getProperty("bodies." + i + ".rot.z", "0.0"));

			double lvX = Double.parseDouble(p.getProperty("bodies." + i + ".lv.x", "0.0"));
			double lvY = Double.parseDouble(p.getProperty("bodies." + i + ".lv.y", "0.0"));
			double lvZ = Double.parseDouble(p.getProperty("bodies." + i + ".lv.z", "0.0"));

			double avX = Double.parseDouble(p.getProperty("bodies." + i + ".av.x", "0.0"));
			double avY = Double.parseDouble(p.getProperty("bodies." + i + ".av.y", "0.0"));
			double avZ = Double.parseDouble(p.getProperty("bodies." + i + ".av.z", "0.0"));

			s.setMass(mass);
			s.setLocation(new Vector(locX, locY, locZ));
			s.setRotation(new Vector(rotX, rotY, rotZ));
			s.setLinearVelocity(new Vector(lvX, lvY, lvZ));
			s.setAngularVelocity(new Vector(avX, avY, avZ));

			b.updateModel();

			bodies.add(b);
		}

		double ambient = Double.parseDouble(p.getProperty("light.ambient", "0.0"));

		if (ambient != 0.0) {
			this.addLight(new DirectionalAmbientLight(ambient,
					new RGB(1.0, 1.0, 1.0), new Vector(0.0, -1.0, -0.3)));
		}

		boolean defaultLightRig = Boolean.parseBoolean(p.getProperty("light.default.rig", "false"));
		if (defaultLightRig) {
			StandardLightingRigs.addDefaultLights(this);
		}

		String viewFrom = p.getProperty("camera.view.from");
		String viewTo = p.getProperty("camera.view.to");

		if (viewFrom != null && viewTo != null) {
			int from = Integer.parseInt(viewFrom);
			int to = Integer.parseInt(viewTo);

			Vector vd = ((RigidBody)bodies.get(to)).getState().getLocation().subtract(
					((RigidBody)bodies.get(from)).getState().getLocation());
			vd.divideBy(vd.length());

			Vector l = vd.multiply(((AbstractSurface)bodies.get(from)).getSize() * 1.05);

			c.setLocation(l);
			c.setViewDirection(vd);
		}

		setCamera(c);
		addAll(bodies);
	}
}
