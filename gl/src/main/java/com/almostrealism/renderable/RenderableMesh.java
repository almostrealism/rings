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

package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import com.almostrealism.gl.GLPrintWriter;
import com.jogamp.opengl.GL2;
import io.almostrealism.lang.CodePrintWriter; //this is not good - remove it - Kristen added for experiment
import io.almostrealism.code.ExpressionAssignment;
import io.almostrealism.expression.Expression;
import io.almostrealism.expression.StaticReference;
import io.almostrealism.scope.Method;
import io.almostrealism.scope.Scope;
import io.almostrealism.scope.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.almostrealism.geometry.TransformMatrix;
import org.almostrealism.algebra.Vector;

import org.almostrealism.space.Mesh;
import org.almostrealism.space.Triangle;
import org.almostrealism.CodeFeatures;

public class RenderableMesh extends RenderableGeometry<Mesh> implements CodeFeatures {
	public RenderableMesh(Mesh m) { super(m); }
	
	@Override
	public void init(GLDriver gl) {
		List<Double> vertices = new ArrayList<>();
		for (Triangle t : getGeometry().triangles()) {
			Vector v[] = t.getVertices();
			Vector n[] = new Vector[] {
						t.getNormalAt(v(v[0])).get().evaluate(),
						t.getNormalAt(v(v[1])).get().evaluate(),
						t.getNormalAt(v(v[2])).get().evaluate()};

			vertices.add(v[0].getX());
			vertices.add(v[0].getY());
			vertices.add(v[0].getZ());
			vertices.add(n[0].getX());
			vertices.add(n[0].getY());
			vertices.add(n[0].getZ());
			vertices.add(v[1].getX());
			vertices.add(v[1].getY());
			vertices.add(v[1].getZ());
			vertices.add(n[1].getX());
			vertices.add(n[1].getY());
			vertices.add(n[1].getZ());
			vertices.add(v[2].getX());
			vertices.add(v[2].getY());
			vertices.add(v[2].getZ());
			vertices.add(n[2].getX());
			vertices.add(n[2].getY());
			vertices.add(n[2].getZ());
		}

		Expression buffer = gl.createBuffer();
		gl.bindBuffer("ARRAY_BUFFER", buffer);
		gl.bufferData(buffer, vertices);
	} 

	@Override
	public void render(GLDriver gl) {

		
		gl.glMaterial(getMaterial());

		gl.glBegin(GL2.GL_TRIANGLES);

	
		for (Triangle t : getGeometry().triangles()) {
			
			gl.triangle(t);
		}

		gl.glEnd();
	}

//	@Override
//	public void write(String glMember, String name, CodePrintWriter p) {
//		ResourceVariable v = new ResourceVariable(name + "Mesh", new MeshResource(getGeometry()));
//		p.println(v);
//		// TODO  Render mesh
//		
//		
//	}
	
	//Added this and stopped here Saturday
	@Override
	public void display(GLDriver gl) {
		System.out.println("display method of RenderableMesh");
		super.display(gl);
		experimental(gl);
	}
	
	//This is doing violence to the design of course.  Just an experiment.
	private void experimental(GLDriver gl) {
		String glMember = "gl";
		List<String> emptyList = new ArrayList<String>();
		Map<String,Variable> emptyMap = new HashMap<String,Variable>();

		ExpressionAssignment positionBuffer = declare("positionBuffer", new Method(glMember, "createBuffer"));
		
		GLPrintWriter glPrintWriter = (GLPrintWriter)gl;
		CodePrintWriter p = glPrintWriter.getPrintWriter();
		p.println(positionBuffer);
		
		List<Expression<?>> bindArgs = new ArrayList<>();
		bindArgs.add(glPrintWriter.glParam("ARRAY_BUFFER"));
		bindArgs.add(new StaticReference<>(null, "positionBuffer"));
		
		Method bindBuf = glPrintWriter.glMethod("bindBuffer", bindArgs);
		
		p.println(bindBuf);


		ExpressionAssignment positions = declare("positions", new StaticReference<>(String.class, "[1.0,1.0,-1.0,1.0,1.0,-1.0,-1.0,-1.0]"));
		p.println(positions);
		
		
		
		p.println(glPrintWriter.glMethod("bufferData", glPrintWriter.glParam("ARRAY_BUFFER"),
				new StaticReference<>(null, "new Float32Array(positions)"),
				glPrintWriter.glParam("STATIC_DRAW")));
		
		//var buffers = { position: positionBuffer, };

		ExpressionAssignment buffers = declare("buffers", new StaticReference<>(String.class, "{ position: positionBuffer, }"));
		p.println(buffers);
		Scope<Variable> bufferBinding = new Scope<Variable>();
		List<ExpressionAssignment<?>> vars = bufferBinding.getVariables();
		ExpressionAssignment numC = declare("numComponents", e(2));
		vars.add(numC);

		ExpressionAssignment norm = declare("normalize", e(false));
		vars.add(norm);
		ExpressionAssignment strd = declare("stride", e(0));
		vars.add(strd);
		ExpressionAssignment offs = declare("offset", e(0));
		vars.add(offs);
		List<Method> methods = bufferBinding.getMethods();
		methods.add(glPrintWriter.glMethod("bindBuffer", glPrintWriter.glParam("ARRAY_BUFFER"),
				new StaticReference<>(Vector.class, "buffers.position")));
		methods.add(glPrintWriter.glMethod("vertexAttribPointer",
				new StaticReference<>(Vector.class, "programInfo.attribLocations.vertexPosition"),
				numC.getDestination(),
				glPrintWriter.glParam("FLOAT"), norm.getDestination(),
				strd.getDestination(), offs.getDestination()));
		
		//add
//		gl.enableVertexAttribArray(
//		        programInfo.attribLocations.vertexPosition);
		methods.add(glPrintWriter.glMethod("enableVertexAttribArray",
				new StaticReference<>(Vector.class, "programInfo.attribLocations.vertexPosition")));
		
		for (Iterator<ExpressionAssignment<?>> it = vars.iterator(); it.hasNext();) {
			ExpressionAssignment v = it.next();
			p.println(v);
		}
		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
			Method method = (Method) iterator.next();
			p.println(method);
		}
		
		Method useP = glPrintWriter.glMethod("useProgram", new StaticReference<>(null, "programInfo.program"));
		p.println(useP);
		
		Method m4fv = glPrintWriter.glMethod("uniformMatrix4fv",
				new StaticReference<>(TransformMatrix.class, "programInfo.uniformLocations.projectionMatrix"),
				norm.getDestination(), new StaticReference<>(TransformMatrix.class, "projectionMatrix"));
		Method m4fvModel = glPrintWriter.glMethod("uniformMatrix4fv",
				new StaticReference<>(TransformMatrix.class, "programInfo.uniformLocations.modelViewMatrix"),
				norm.getDestination(),
				new StaticReference<>(TransformMatrix.class, "modelViewMatrix"));

		//pass identity matrices instead because conversion is on server side
//		p.println(m4fv);
//		p.println(m4fvModel);

		ExpressionAssignment vCount = declare("vertexCount", e(4));
		Method drawIt = glPrintWriter.glMethod("drawArrays", glPrintWriter.glParam("TRIANGLE_STRIP"),
				offs.getDestination(), vCount.getDestination());
		p.println(vCount);
		p.println(drawIt);
	}
}
