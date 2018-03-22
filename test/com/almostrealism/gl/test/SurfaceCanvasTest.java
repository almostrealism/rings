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

import com.almostrealism.gl.DefaultGLCanvas;
import com.jogamp.opengl.util.texture.Texture;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.graph.io.PlyResource;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.junit.Test;

import com.almostrealism.gl.SurfaceCanvas;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.primitives.Sphere;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author  Michael Murray
 */
public class SurfaceCanvasTest {
	@Test
	public void test() throws IOException {
		Scene<ShadableSurface> scene = new Scene<>();
		scene.setCamera(new PinholeCamera());

		PlyResource p = new PlyResource(new File("dragon.ply"));
		PlyResource.MeshReader t = new PlyResource.MeshReader();

//		scene.add(t.transcode(p).getMesh());
		scene.add(new Sphere(new Vector(-5, 0, 0), 1));
		scene.add(new Sphere(new Vector(5, 0, 0), 3));

		SurfaceCanvas c = new SurfaceCanvas(scene, getClass().getClassLoader(),"uffizi_","png",true);
		c.autoPositionCamera();
		
		JFrame frame = new JFrame("Test");
		frame.setSize(400, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(c);
		frame.setVisible(true);
		
		c.start();
	}
	
	public static void main(String args[]) throws IOException {
		new SurfaceCanvasTest().test();
	}
}
