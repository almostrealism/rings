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

import com.almostrealism.gl.DefaultGLCanvas;
import com.almostrealism.renderable.Gear;
import org.almostrealism.algebra.Vector;
import org.almostrealism.projection.PinholeCamera;
import org.junit.Test;

import javax.swing.*;

/**
 * @author  Michael Murray
 */
public class DefaultGLCanvasTest {
	@Test
	public void test() {
		DefaultGLCanvas c = new DefaultGLCanvas() {
			@Override
			public PinholeCamera getCamera() {
				PinholeCamera c = new PinholeCamera();
				c.setLocation(new Vector(0.0, 0.0, -40.0));
				return c;
			}
		};
		
		c.add(new Gear(1.0f, 4.0f, 1.0f, 20, 0.7f));
		
		JFrame frame = new JFrame("Test");
		frame.setSize(300, 300);
		frame.getContentPane().add(c);
		frame.setVisible(true);
		
		c.start();
	}
	
	public static void main(String[] args) { new DefaultGLCanvasTest().test(); }
}
