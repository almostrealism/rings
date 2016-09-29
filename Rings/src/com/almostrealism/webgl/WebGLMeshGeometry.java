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

package com.almostrealism.webgl;

import org.almostrealism.html.HTMLContent;

import com.almostrealism.raytracer.primitives.Mesh;

public class WebGLMeshGeometry implements WebGLExportable {
	private Mesh.VertexData data;
	
	public WebGLMeshGeometry(Mesh.VertexData data) { this.data = data; }
	
	@Override
	public HTMLContent getWebGLContent() {
		return () -> {
			StringBuffer buf = new StringBuffer();
			buf.append("var geometry = new THREE.Geometry();\n");
			buf.append("geometry.vertices.push(");
			
			for (int i = 0; i < data.getVertexCount(); i++) {
				buf.append(asThreeVector(data, i));
				if (i < (data.getVertexCount() - 1)) buf.append(",");
				buf.append("\n");
			}
			
			buf.append(");\n");
			
			buf.append("geometry.faces.push(");
			
			for (int i = 0; i < data.getTriangleCount(); i++) {
				buf.append(asThreeFace(data.getTriangle(i)));
				if (i < (data.getTriangleCount() - 1)) buf.append(",");
				buf.append("\n");
			}
			
			buf.append(");\n");
			
			return buf.toString();
		};
	}
	
	private static String asThreeVector(Mesh.VertexData v, int index) {
		return "new THREE.Vector3(" + v.getX(index) + ", " + v.getY(index) + ", " + v.getZ(index) + ")";
	}
	
	private static String asThreeFace(int triangle[]) {
		return "new THREE.Face3(" + triangle[0] + ", " + triangle[1] + ", " + triangle[2] + ")";
	}
}
