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

package com.almostrealism.gl;

import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableSurfaceFactory;
import io.almostrealism.expression.Expression;
import io.almostrealism.lang.CodePrintWriter;
import org.almostrealism.color.RGBA;
import org.almostrealism.color.ShadableSurface;
import org.almostrealism.projection.OrthographicCamera;
import org.almostrealism.space.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO  It may be unnecessary to have this class and {@link com.almostrealism.renderable.RenderableList}
 */
public class GLScene extends ArrayList<Renderable> implements Renderable {
	public static final boolean verbose = false;

	private static final String VERTEX_SHADER = "    precision mediump float;\n" +
			"\n" +
			"    attribute vec3 pos;\n" +
			"    attribute vec3 normal;\n" +
			"\n" +
			"    varying vec3 col;\n" +
			"\n" +
			"    uniform mat4 projectionMatrix, viewMatrix, modelMatrix;\n" +
			"    uniform mat3 normalMatrix;\n" +
			"\n" +
			"    uniform vec3 ambientLightColour, directionalLight, materialSpecular;\n" +
			"    uniform float materialAmbient, materialDiffuse, shininess;\n" +
			"\n" +
			"    /* A function to determine the colour of a vertex, accounting\n" +
			"       for ambient and directional light */\n" +
			"    vec3 ads( vec4 position, vec3 norm )\n" +
			"    {\n" +
			"      vec3 s = normalize(vec3(vec4(directionalLight,1.0) - position));\n" +
			"      vec3 v = normalize(vec3(-position));\n" +
			"      vec3 r = reflect(-s, norm);\n" +
			"      return ambientLightColour +\n" +
			"        materialDiffuse * max(dot(s,norm), 0.0) +\n" +
			"        materialSpecular * pow(max(dot(r,v), 0.0), shininess);\n" +
			"    }\n" +
			"\n" +
			"    void main() {\n" +
			"      vec3 eyeNormal = normalize(normalMatrix * normal);\n" +
			"      vec4 eyePosition =  viewMatrix * modelMatrix * vec4(pos, 1.0);\n" +
			"      col = min(vec3(0.0) + ads(eyePosition, eyeNormal), 1.0);\n" +
			"      gl_Position = projectionMatrix * viewMatrix * modelMatrix *\n" +
			"        vec4(pos, 1.0);\n" +
			"    }";

	public static String FRAGMENT_SHADER = "    precision mediump float;\n" +
			"\n" +
			"    varying vec3 col;\n" +
			"\n" +
			"    void main() {\n" +
			"      gl_FragColor = vec4(col, 1.0);\n" +
			"    }";

	private final Scene<ShadableSurface> scene;
	private final List<ShadableSurface> added;

	public GLScene(Scene<ShadableSurface> s) {
		this.scene = s;
		this.added = new ArrayList<>();
	}

	public Scene<ShadableSurface> getScene() { return scene; }

	public int getFPS() { return 80; } // TODO  Should be assignable

	public OrthographicCamera getCamera() { return (OrthographicCamera) getScene().getCamera(); }

	@Override
	public void init(GLDriver gl) {
		s: for (ShadableSurface s : this.scene) {
			if (this.added.contains(s)) continue s;
			add(RenderableSurfaceFactory.createRenderableSurface(s));
			added.add(s);
		}

		Expression program = gl.createProgram();
		Expression shader = gl.createShader("VERTEX_SHADER");

		gl.shaderSource(shader, VERTEX_SHADER.replaceAll("\n", " "));
		gl.compileShader(shader);
		gl.attachShader(program, shader);
		gl.deleteShader(shader);

		shader = gl.createShader("FRAGMENT_SHADER");
		gl.shaderSource(shader, FRAGMENT_SHADER.replaceAll("\n", " "));
		gl.compileShader(shader);
		gl.attachShader(program, shader);
		gl.deleteShader(shader);

		gl.linkProgram(program);
		gl.useProgram(program);

		gl.mapProgramAttributes(program);

		List<Renderable> rs = new ArrayList<>();
		rs.addAll(this);

		for (Renderable r : rs) r.init(gl);
	}

	@Override
	public void display(GLDriver gl) {
		gl.glClearColor(new RGBA(0.0, 0.0, 0.0, 1.0));
		gl.clearDepth(1.0);
		gl.enable("DEPTH_TEST");
		gl.glDepthFunc("LEQUAL");
		gl.glClearColorAndDepth();
		
		List<Renderable> rs = new ArrayList<>();
		rs.addAll(this);

		r: for (Renderable r : rs) {
			if (r == null) continue r;
			if (verbose) System.out.println("Rendering " + r);
			gl.pushCamera();
			gl.pushLighting();
			gl.setLighting(new GLLightingConfiguration(getScene().getLights()));
			gl.setCamera(getCamera());
			r.display(gl);
			gl.popLighting();
			gl.popCamera();
			if (verbose) System.out.println("Done rendering " + r);
		}
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {

		int index = 0;
		for (Renderable r : this) { r.write(glMember, name + (index++), p); }
	}
}
