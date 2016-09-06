/*
 * Copyright 2016 Michael Murray
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

package com.almostrealism.replicator.test;

import javax.swing.JFrame;

import org.almostrealism.color.RGB;
import org.almostrealism.space.Vector;
import org.junit.Test;

import com.almostrealism.gl.SurfaceCanvas;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.primitives.Sphere;
import com.almostrealism.replicator.ReplicatorTableModel;

/**
 * @author  Michael Murray
 */
public class ReplicatorTableModelTest {
	@Test
	public void test() {
		PinholeCamera camera = new PinholeCamera();
		camera.setLocation(new Vector(0.0, 0.0, -40.0));
		
		ReplicatorTableModel model = new ReplicatorTableModel();
		
		Sphere s = new Sphere(2.0);
		s.setColor(RGB.gray(0.8));
		model.addLayer("Sphere", s);
		model.setCamera(camera);
		
		SurfaceCanvas c = new SurfaceCanvas(model);
		
		JFrame frame = new JFrame("Test");
		frame.setSize(300, 300);
		frame.getContentPane().add(c);
		frame.setVisible(true);
		
		c.start();
	}
	
	public static void main(String args[]) { new ReplicatorTableModelTest().test(); }
}
