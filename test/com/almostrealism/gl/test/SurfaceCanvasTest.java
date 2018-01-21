/*
 * Copyright 2017 Michael Murray
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
import org.almostrealism.color.RGB;
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
		camera.setLocation(new Vector(0.0, 0.0, -40.0));
		
		Scene<ShadableSurface> scene = new Scene<ShadableSurface>();
		scene.setCamera(camera);
		
		Sphere s = new Sphere(2.0);
		s.setColor(RGB.gray(0.8));
		scene.add(new Sphere(2.0));
		
		SurfaceCanvas c = new SurfaceCanvas(scene);
		
		JFrame frame = new JFrame("Test");
		frame.setSize(300, 300);
		frame.getContentPane().add(c);
		frame.setVisible(true);
		
		c.start();
	}
	
	public static void main(String args[]) { new SurfaceCanvasTest().test(); }
}
