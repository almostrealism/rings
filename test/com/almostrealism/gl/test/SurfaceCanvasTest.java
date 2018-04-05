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

package com.almostrealism.gl.test;

import javax.swing.JFrame;

import org.almostrealism.algebra.Vector;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.junit.Test;

import com.almostrealism.gl.SurfaceCanvas;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.primitives.Sphere;

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

		scene.add(new Sphere(new Vector(0, 1000, 0), 50));
		scene.add(new Sphere(new Vector(200, 1000, 0), 50));
		scene.add(new Sphere(new Vector(200, 1200, 0), 50));
		scene.add(new Sphere(new Vector(200, 1200, 200), 50));

		SurfaceCanvas c = new SurfaceCanvas(scene);
		c.autoPositionCamera(new Vector(-1,-0.5,-1));

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