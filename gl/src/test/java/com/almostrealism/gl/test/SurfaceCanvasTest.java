/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.gl.test;

import javax.swing.JFrame;

import com.almostrealism.lighting.PointLight;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.junit.Test;

import com.almostrealism.gl.SurfaceCanvas;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.primitives.Sphere;

/**
 * @author  Michael Murray
 */
public class SurfaceCanvasTest {
	@Test
	public void test() {
		PinholeCamera camera = new PinholeCamera();
		Scene<ShadableSurface> scene = new Scene<>();
		scene.setCamera(camera);

		// 36 x 24mm film & 50mm focal length.
		camera.setProjectionDimensions(36, 24);
		camera.setFocalLength(50);

		scene.addLight(new PointLight(new Vector(0.0, 1.0, 0.0), 1.0, new RGB(0.8, 0.2, 0.1)));
		scene.addLight(new PointLight(new Vector(1.0, 1.0, -3.0), 1.0, new RGB(0.3, 0.9, 0.2)));
		scene.addLight(new PointLight(new Vector(0, 0, 20), 1, new RGB(1, 1, 1)));

		// Test Spheres
		Sphere s0 = new Sphere(new Vector(10, 10, 10), 1);
		s0.setColor(new RGB(1, 1, 1));
		scene.add(s0);

		Sphere s = new Sphere(new Vector(0, 0, 0), 5);
		s.setColor(new RGB(1, 0, 0));
		scene.add(s);

		Sphere s2 = new Sphere(new Vector(20, 0, 0), 5);
		s2.setColor(new RGB(0, 1, 0));
		scene.add(s2);

		Sphere s3 = new Sphere(new Vector(20, 20, 0), 5);
		s3.setColor(new RGB(0, 0, 1));
		scene.add(s3);

		Sphere s4 = new Sphere(new Vector(20, 20, 20), 5);
		s4.setColor(new RGB(1, 1, 0));
		scene.add(s4);

		SurfaceCanvas c = new SurfaceCanvas(scene);
		c.autoPositionCamera(new Vector(1,-0.5,-1));

		JFrame frame = new JFrame("Test");
		frame.setSize(670, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(c);
		frame.setVisible(true);

		c.start();
	}

	public static void main(String args[]) {
		new SurfaceCanvasTest().test();
	}
}