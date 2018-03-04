/* San Angeles Observation OpenGL ES version example
 * Copyright 2004-2005 Jetro Lauha
 * All rights reserved.
 * Web: http://iki.fi/jetro/
 *
 * This source is free software; you can redistribute it and/or
 * modify it under the terms of EITHER:
 *   (1) The GNU Lesser General Public License as published by the Free
 *       Software Foundation; either version 2.1 of the License, or (at
 *       your option) any later version. The text of the GNU Lesser
 *       General Public License is included with this source in the
 *       file LICENSE-LGPL.txt.
 *   (2) The BSD-style license that is included with this source in
 *       the file LICENSE-BSD.txt.
 *
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files
 * LICENSE-LGPL.txt and LICENSE-BSD.txt for more details.
 *
 * $Id$
 * $Revision$
 */

package com.almostrealism.gl;

import com.almostrealism.renderable.GLDriver;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;

public class SuperShape extends GLSpatial {
	public static final int PARAMS = 15;

	public static final float sParams[][] =
			{
					// m  a     b     n1      n2     n3     m     a     b     n1     n2      n3   res1 res2 scale  (org.res1,res2)
					new float[]{10, 1, 2, 90, 1, -45, 8, 1, 1, -1, 1, -0.4f, 20, 30, 2}, // 40, 60
					new float[]{10, 1, 2, 90, 1, -45, 4, 1, 1, 10, 1, -0.4f, 20, 20, 4}, // 40, 40
					new float[]{10, 1, 2, 60, 1, -10, 4, 1, 1, -1, -2, -0.4f, 41, 41, 1}, // 82, 82
					new float[]{6, 1, 1, 60, 1, -70, 8, 1, 1, 0.4f, 3, 0.25f, 20, 20, 1}, // 40, 40
					new float[]{4, 1, 1, 30, 1, 20, 12, 1, 1, 0.4f, 3, 0.25f, 10, 30, 1}, // 20, 60
					new float[]{8, 1, 1, 30, 1, -4, 8, 2, 1, -1, 5, 0.5f, 25, 26, 1}, // 60, 60
					new float[]{13, 1, 1, 30, 1, -4, 13, 1, 1, 1, 5, 1, 30, 30, 6}, // 60, 60
					new float[]{10, 1, 1.1f, -0.5f, 0.1f, 70, 60, 1, 1, -90, 0, -0.25f, 20, 60, 8}, // 60, 180
					new float[]{7, 1, 1, 20, -0.3f, -3.5f, 6, 1, 1, -1, 4.5f, 0.5f, 10, 20, 4}, // 60, 80
					new float[]{4, 1, 1, 10, 10, 10, 4, 1, 1, 10, 10, 10, 10, 20, 1}, // 20, 40
					new float[]{4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 10, 10, 2}, // 10, 10
					new float[]{1, 1, 1, 38, -0.25f, 19, 4, 1, 1, 10, 10, 10, 10, 15, 2}, // 20, 40
					new float[]{2, 1, 1, 0.7f, 0.3f, 0.2f, 3, 1, 1, 100, 100, 100, 10, 25, 2}, // 20, 50
					new float[]{6, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 30, 30, 2}, // 60, 60
					new float[]{3, 1, 1, 1, 1, 1, 6, 1, 1, 2, 1, 1, 10, 20, 2}, // 20, 40
					new float[]{6, 1, 1, 6, 5.5f, 100, 6, 1, 1, 25, 10, 10, 30, 20, 2}, // 60, 40
					new float[]{3, 1, 1, 0.5f, 1.7f, 1.7f, 2, 1, 1, 10, 10, 10, 20, 20, 2}, // 40, 40
					new float[]{5, 1, 1, 0.1f, 1.7f, 1.7f, 1, 1, 1, 0.3f, 0.5f, 0.5f, 20, 20, 4}, // 40, 40
					new float[]{2, 1, 1, 6, 5.5f, 100, 6, 1, 1, 4, 10, 10, 10, 22, 1}, // 40, 40
					new float[]{6, 1, 1, -1, 70, 0.1f, 9, 1, 0.5f, -98, 0.05f, -45, 20, 30, 4}, // 60, 91
					new float[]{6, 1, 1, -1, 90, -0.1f, 7, 1, 1, 90, 1.3f, 34, 13, 16, 1}, // 32, 60
			};

	/**
	 * Creates and returns a supershape object. Based on Paul Bourke's POV-Ray implementation.
     * http://astronomy.swin.edu.au/~pbourke/povray/supershape/
	 */
	public SuperShape(GLDriver gl, SuperShapeParams params) {
		super(gl, params.vertices, 3, true);

		for (params.a = 0; params.a < 3; ++params.a) {
			params.baseColor[params.a] = ((DefaultGLCanvas.randomUInt() % 155) + 100) / 255.f;
		}

		params.currentQuad = 0;
		params.currentVertex = 0;

		// longitude -pi to pi
		for (params.longitude = 0; params.longitude < params.longitudeCount; ++params.longitude) {

			// latitude 0 to pi/2
			for (params.latitude = params.latitudeBegin; params.latitude < params.latitudeEnd; ++params.latitude) {
				float t1 = (float) (-Math.PI + params.longitude * 2 * Math.PI / params.resol1);
				float t2 = (float) (-Math.PI + (params.longitude + 1) * 2 * Math.PI / params.resol1);
				float p1 = (float) (-Math.PI / 2 + params.latitude * 2 * Math.PI / params.resol2);
				float p2 = (float) (-Math.PI / 2 + (params.latitude + 1) * 2 * Math.PI / params.resol2);
				float r0, r1, r2, r3;

				r0 = Scalar.ssFunc(t1, params.params);
				r1 = Scalar.ssFunc(p1, params.params, 6);
				r2 = Scalar.ssFunc(t2, params.params);
				r3 = Scalar.ssFunc(p2, params.params, 6);

				if (r0 != 0 && r1 != 0 && r2 != 0 && r3 != 0) {
					Vector pa = new Vector(), pb = new Vector(), pc = new Vector(), pd = new Vector();
					Vector v1 = new Vector(), v2 = new Vector(), n = new Vector();
					double ca;
					int i;
					//float lenSq, invLenSq;

					SuperShape.superShapeMap(pa, r0, r1, t1, p1);
					SuperShape.superShapeMap(pb, r2, r1, t2, p1);
					SuperShape.superShapeMap(pc, r2, r3, t2, p2);
					SuperShape.superShapeMap(pd, r0, r3, t1, p2);

					// kludge to set lower edge of the object to fixed level
					if (params.latitude == params.latitudeBegin + 1) {
						pa.setZ(0.0);
						pb.setZ(0.0);
					}

					Vector.vector3Sub(v1, pb, pa);
					Vector.vector3Sub(v2, pd, pa);

					// Calculate normal with cross product.
				/*   i    j    k      i    j
                 * v1.x v1.y v1.z | v1.x v1.y
                 * v2.x v2.y v2.z | v2.x v2.y
                 */

					n.setX(v1.getY() * v2.getZ() - v1.getZ() * v2.getY());
					n.setY(v1.getZ() * v2.getX() - v1.getX() * v2.getZ());
					n.setZ(v1.getX() * v2.getY() - v1.getY() * v2.getX());

                /* Pre-normalization of the normals is disabled here because
                 * they will be normalized anyway later due to automatic
                 * normalization (GL2ES1.GL_NORMALIZE). It is enabled because the
                 * objects are scaled with glScale.
                 */
                /*
                lenSq = n.x * n.x + n.y * n.y + n.z * n.z;
                invLenSq = (float)(1 / sqrt(lenSq));
                n.x *= invLenSq;
                n.y *= invLenSq;
                n.z *= invLenSq;
                */

					ca = pa.getZ() + 0.5;

					if (normalArray != null) {
						for (i = params.currentVertex * 3;
							 	i < (params.currentVertex + 6) * 3;
							 	i += 3) {
							normalArray.put(i, (float) (n.getX()));
							normalArray.put(i + 1, (float) (n.getY()));
							normalArray.put(i + 2, (float) (n.getZ()));
						}
					}

					for (i = params.currentVertex * DefaultGLCanvas.cComps;
						 	i < (params.currentVertex + 6) * DefaultGLCanvas.cComps;
						 	i += DefaultGLCanvas.cComps) {
						int j;
						float color[] = new float[3];
						for (j = 0; j < 3; ++j) {
							color[j] = (float) (ca * params.baseColor[j]);
							if (color[j] > 1.0f) color[j] = 1.0f;
						}

						colorArray.put(i, color[0]);
						colorArray.put(i + 1, color[1]);
						colorArray.put(i + 2, color[2]);
						if (3 < DefaultGLCanvas.cComps) {
							colorArray.put(i + 3, 0f);
						}
					}

					vertexArray.put(params.currentVertex * 3, (float) (pa.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pa.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pa.getZ()));
					++params.currentVertex;

					vertexArray.put(params.currentVertex * 3, (float) (pb.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pb.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pb.getZ()));
					++params.currentVertex;

					vertexArray.put(params.currentVertex * 3, (float) (pd.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pd.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pd.getZ()));
					++params.currentVertex;

					vertexArray.put(params.currentVertex * 3, (float) (pb.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pb.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pb.getZ()));
					++params.currentVertex;

					vertexArray.put(params.currentVertex * 3, (float) (pc.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pc.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pc.getZ()));
					++params.currentVertex;

					vertexArray.put(params.currentVertex * 3, (float) (pd.getX()));
					vertexArray.put(params.currentVertex * 3 + 1, (float) (pd.getY()));
					vertexArray.put(params.currentVertex * 3 + 2, (float) (pd.getZ()));
					++params.currentVertex;
				} // r0 && r1 && r2 && r3

				++params.currentQuad;
			} // latitude
		} // longitude

		// Set number of vertices in object to the actual amount created.
		setCount(params.currentVertex);
		seal(gl);
	}

	public static void superShapeMap(Vector point, float r1, float r2, float t, float p) {
		// sphere-mapping of supershape parameters
		point.setX((Math.cos(t) * Math.cos(p) / r1 / r2));
		point.setY((Math.sin(t) * Math.cos(p) / r1 / r2));
		point.setZ((Math.sin(p) / r2));
	}

	public static class SuperShapeParams {
		float params[];
		int resol1;
		int resol2;
		// latitude 0 to pi/2 for no mirrored bottom
		// (latitudeBegin==0 for -pi/2 to pi/2 originally)
		int latitudeBegin = resol2 / 4;
		int latitudeEnd = resol2 / 2;    // non-inclusive
		int longitudeCount = resol1;
		int latitudeCount = latitudeEnd - latitudeBegin;
		int triangleCount = longitudeCount * latitudeCount * 2;
		int vertices = triangleCount * 3;
		float baseColor[] = new float[3];
		int a, longitude, latitude;
		int currentVertex, currentQuad;

		public SuperShapeParams(float params[]) {
			this.params = params;
			resol1 = (int) params[SuperShape.PARAMS - 3];
			resol2 = (int) params[SuperShape.PARAMS - 2];
			// latitude 0 to pi/2 for no mirrored bottom
			// (latitudeBegin==0 for -pi/2 to pi/2 originally)
			latitudeBegin = resol2 / 4;
			latitudeEnd = resol2 / 2;    // non-inclusive
			longitudeCount = resol1;
			latitudeCount = latitudeEnd - latitudeBegin;
			triangleCount = longitudeCount * latitudeCount * 2;
			vertices = triangleCount * 3;
			baseColor = new float[3];
		}
	}
}
