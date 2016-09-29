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

package com.almostrealism.explorer.models;

import java.util.ArrayList;

import com.almostrealism.raytracer.primitives.Mesh.VertexData;

class TerrainVertexData implements VertexData {
	private int resolution;
	private double width, height;

	private double vertices[][];
	private int triangles[][];
	private double textures[][];

	public TerrainVertexData(int resolution, double width, double height) {
		this.resolution = resolution;
		this.width = width;
		this.height = height;

		populateMesh();
	}

	@Override
	public double getRed(int index) { return 1.0; }

	@Override
	public double getGreen(int index) { return 1.0; }

	@Override
	public double getBlue(int index) { return 1.0; }

	@Override
	public double getX(int index) { return vertices[index][0]; }

	@Override
	public double getY(int index) { return vertices[index][1]; }

	@Override
	public double getZ(int index) { return vertices[index][2]; }

	@Override
	public double getTextureU(int index) { return textures[index][0]; }

	@Override
	public double getTextureV(int index) { return textures[index][1]; }

	@Override
	public int[] getTriangle(int index) { return triangles[index]; }

	@Override
	public int getTriangleCount() { return triangles.length; }
	
	@Override
	public int getVertexCount() { return vertices.length; }
	
	protected void populateMesh() {
		// center x,z on origin
		double offset = width / 2.0f;
		double scalex = width / (float) resolution;
		double scalez = height / (float) resolution;
		
		vertices = new double[resolution * resolution][3];
		textures = new double[resolution * resolution][3];
		
		for (int z = 0; z < resolution; z++) {
			for (int x = 0; x < resolution; x++) {
				int index = x + (z * resolution);
				vertices[index][0] = (scalex * x) - offset; 
				vertices[index][1] = 0.0; // height 
				vertices[index][2] = (scalez * z) - offset;
				
				textures[index][0] = x / (double) resolution;
				textures[index][1] = z / (double) resolution;
			}
		}
		
		ArrayList<Integer> triangleFan = new ArrayList<Integer>();
		
		for (int z = 0; z < resolution - 1; z++) {
			// degenerate index on non-first row
			if (z != 0) triangleFan.add(z * resolution);
			
			// main strip
			for (int x = 0; x < resolution; x++) {
				triangleFan.add(z * resolution + x);
				triangleFan.add((z + 1) * resolution + x);
			}

			// degenerate index on non-last row
			if (z != (resolution - 2))
				triangleFan.add((z + 1) * resolution + (resolution - 1));
		}
		
		triangles = new int[triangleFan.size() - 2][3];
		
		for (int i = 0; i < triangles.length; i++) {
			if (i % 2 == 0) {
				triangles[i][0] = triangleFan.get(i);
				triangles[i][1] = triangleFan.get(i + 1);
				triangles[i][2] = triangleFan.get(i + 2);
			} else {
				triangles[i][2] = triangleFan.get(i);
				triangles[i][1] = triangleFan.get(i + 1);
				triangles[i][0] = triangleFan.get(i + 2);
			}
		}
	}
}
