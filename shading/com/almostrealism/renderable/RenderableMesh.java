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
import io.almostrealism.code.CodePrintWriter; //this is not good - remove it - Kristen added for experiment
import io.almostrealism.code.Expression;
import io.almostrealism.code.InstanceReference;
import io.almostrealism.code.Method;
import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.almostrealism.algebra.TransformMatrix;
import org.almostrealism.algebra.Vector;
import org.almostrealism.graph.mesh.Mesh;

import org.almostrealism.graph.mesh.Triangle;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.util.Provider;

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

		Variable buffer = gl.createBuffer();
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
		
		Variable positionBuffer = new Variable("positionBuffer", String.class, new Method(glMember, "createBuffer"));
		
		GLPrintWriter glPrintWriter = (GLPrintWriter)gl;
		CodePrintWriter p = glPrintWriter.getPrintWriter();
		p.println(positionBuffer);
		
		List<Expression<?>> bindArgs = new ArrayList<>();
		bindArgs.add(glPrintWriter.glParam("ARRAY_BUFFER"));
		bindArgs.add(new InstanceReference(null, "positionBuffer"));
		
		Method bindBuf = glPrintWriter.glMethod("bindBuffer", bindArgs);
		
		p.println(bindBuf);

		
		Variable positions = new Variable("positions", String.class, "[1.0,1.0,-1.0,1.0,1.0,-1.0,-1.0,-1.0]");
		p.println(positions);
		
		
		
		p.println(glPrintWriter.glMethod("bufferData", glPrintWriter.glParam("ARRAY_BUFFER"),
				new Expression(null, "new Float32Array(positions)"),
				glPrintWriter.glParam("STATIC_DRAW")));
		
		//var buffers = { position: positionBuffer, };
		
		Variable buffers = new Variable("buffers", String.class, "{ position: positionBuffer, }");
		p.println(buffers);
		Scope<Variable> bufferBinding = new Scope<Variable>();
		List<Variable<?>> vars = bufferBinding.getVariables();
		Variable numC = new Variable("numComponents", Integer.class, 2);
		vars.add(numC);

		Variable norm = new Variable("normalize",Boolean.class,false);
		vars.add(norm);
		Variable strd = new Variable("stride",Integer.class,0);
		vars.add(strd);
		Variable offs = new Variable("offset",Integer.class,0);
		vars.add(offs);
		List<Method> methods = bufferBinding.getMethods();
		methods.add(glPrintWriter.glMethod("bindBuffer", glPrintWriter.glParam("ARRAY_BUFFER"),
				new InstanceReference(Vector.class, "buffers.position")));
		methods.add(glPrintWriter.glMethod("vertexAttribPointer",
				new InstanceReference(Vector.class, "programInfo.attribLocations.vertexPosition"),
				new InstanceReference<>(numC),
				glPrintWriter.glParam("FLOAT"), new InstanceReference<>(norm),
				new InstanceReference<>(strd), new InstanceReference<>(offs)));
		
		//add
//		gl.enableVertexAttribArray(
//		        programInfo.attribLocations.vertexPosition);
		methods.add(glPrintWriter.glMethod("enableVertexAttribArray",
				new InstanceReference(Vector.class, "programInfo.attribLocations.vertexPosition")));
		
		for (Iterator it = vars.iterator(); it.hasNext();) {
			Variable v = (Variable) it.next();
			p.println(v);
		}
		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
			Method method = (Method) iterator.next();
			p.println(method);
		}
		
		Method useP = glPrintWriter.glMethod("useProgram", new InstanceReference(null, "programInfo.program"));
		p.println(useP);
		
		Method m4fv = glPrintWriter.glMethod("uniformMatrix4fv",
				new InstanceReference<>(TransformMatrix.class, "programInfo.uniformLocations.projectionMatrix"),
				new InstanceReference<>(norm), new InstanceReference<>(TransformMatrix.class, "projectionMatrix"));
		Method m4fvModel = glPrintWriter.glMethod("uniformMatrix4fv",
				new InstanceReference<>(TransformMatrix.class, "programInfo.uniformLocations.modelViewMatrix"),
				new InstanceReference<>(norm),
				new InstanceReference<>(TransformMatrix.class, "modelViewMatrix"));

		//pass identity matrices instead because conversion is on server side
//		p.println(m4fv);
//		p.println(m4fvModel);
		
		Variable vCount = new Variable("vertexCount",Integer.class,4);
		Method drawIt = glPrintWriter.glMethod("drawArrays", glPrintWriter.glParam("TRIANGLE_STRIP"),
				new InstanceReference<>(offs), new InstanceReference<>(vCount));
		p.println(vCount);
		p.println(drawIt);
	}
}
