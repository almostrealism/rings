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

package com.almostrealism.gl.test;

import com.almostrealism.gl.DefaultGLRenderingEngine;
import com.almostrealism.gl.GLPrintWriter;
import com.almostrealism.gl.GLRenderingEngine;
import com.almostrealism.gl.GLScene;
import com.almostrealism.io.FilePrintWriter;
import org.almostrealism.util.JavaScriptPrintWriter;
import org.almostrealism.io.PrintWriter;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

public class GLPrintWriterTest extends AbstractGLSceneTest {
	@Test
	public void test() {
		Scene<ShadableSurface> s = createTestScene();

		GLRenderingEngine engine = new DefaultGLRenderingEngine();

		try (PrintWriter p = new FilePrintWriter(new File("test.js"))) {
			GLPrintWriter out = new GLPrintWriter("gl", "mat", "main", new JavaScriptPrintWriter(p));
			GLScene gls = new GLScene(s);
			gls.init(out);
			engine.drawRenderables(out, gls, 1.0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
