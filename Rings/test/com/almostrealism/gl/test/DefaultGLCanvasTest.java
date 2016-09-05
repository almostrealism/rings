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

package com.almostrealism.gl.test;

import org.junit.Test;

import com.almostrealism.gl.DefaultGLCanvas;
import com.almostrealism.gl.models.Gear;
import com.almostrealism.projection.PinholeCamera;

/**
 * @author  Michael Murray
 */
public class DefaultGLCanvasTest {
	@Test
	public static void test() {
		DefaultGLCanvas c = new DefaultGLCanvas() {
			@Override
			public PinholeCamera getCamera() {
				PinholeCamera c = new PinholeCamera();
				
				return c;
			}
		};
		
		c.add(new Gear(1.0f, 4.0f, 1.0f, 20, 0.7f));
	}
}
