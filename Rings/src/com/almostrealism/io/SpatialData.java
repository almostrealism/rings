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

package com.almostrealism.io;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import org.almostrealism.space.Surface;
import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;

import com.almostrealism.rayshade.Shader;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.engine.ShadableSurfaceWrapper;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.raytracer.primitives.Triangle;

/**
 * @author  Michael Murray
 */
public class SpatialData {
	/** The integer code for a GTS encoding. */
	public static final int GTSEncoding = 8;

	/** The integer code for a ply encoding. */
	public static final int PLYEncoding = 16;

	public static Scene decodeScene(InputStream fileIn, int encoding,
			boolean ui, ExceptionListener listener) throws IOException {
		return SpatialData.decodeScene(fileIn, encoding, ui, listener, null);
	}

	/**
	 * Decodes the scene data read from the specified InputStream object using the encoding specified by
	 * the integer encoding code and returnes the new Scene object. If ui is true only SurfaceUI objects will be used.
	 * The specified ExceptionListener is notified if an exception occurs when using an XMLDecoder.
	 * This method returns null if the encoding is not supported.
	 */
	public static Scene decodeScene(InputStream fileIn, int encoding, boolean ui, ExceptionListener listener, ShadableSurface s) throws IOException {
		if (encoding == FileDecoder.XMLEncoding) {
			XMLDecoder decoder = new XMLDecoder(fileIn);
			decoder.setExceptionListener(listener);

			Scene scene = (Scene)decoder.readObject();

			if (!ui) {
				Surface sr[] = scene.getSurfaces();
				for (int i = 0; i < sr.length; i++)
					if (sr[i] instanceof ShadableSurfaceWrapper)
						sr[i] = ((ShadableSurfaceWrapper) sr[i]).getSurface();
			}

			return scene;
		} else if (encoding == FileDecoder.RAWEncoding) {
			Scene scene = new Scene();

			BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));

			String line = in.readLine();
			int lineCount = 0;

			t: while (line != null) {
				if (line.startsWith("#") == true) {
					line = in.readLine();
					lineCount++;
				} else {
					break t;
				}
			}

			boolean polyData = false;
			int pointCount = 0, polyCount = 0;

			if (line.indexOf(" ", line.indexOf(" ") + 1) < 0) {
				polyData = true;

				pointCount = Integer.parseInt(line.substring(0, line.indexOf(" ")));
				polyCount = Integer.parseInt(line.substring(line.indexOf(" ") + 1));

				line = in.readLine();
				lineCount++;
			}

			l: while (line != null) {
				int tCount = 0;

				if (line.startsWith("#") == true) {
					line = in.readLine();
					lineCount++;

					continue l;
				}

				if (polyData == true) {
					Vector points[] = new Vector[pointCount];

					for (int p = 0; p < points.length; p++) {
						t: while (true) {
							if (line.startsWith("#") == true) {
								line = in.readLine();
								lineCount++;
							} else {
								break t;
							}
						}

					double data[] = new double[3];

					boolean broken = false;

					i: for (int i = 0; i < data.length; i++) {
						int index = line.indexOf(" ");

						if (i == data.length - 1) {
							data[i] = Double.parseDouble(line);

							break i;
						}

						if (index > 0) {
							data[i] = Double.parseDouble(line.substring(0, index));
							line = line.substring(index + 1);
						} else {
							System.out.println("Line " + lineCount + " of RAW file does not contain the proper number of terms, " +
									"or is not properly delimited");

							broken = true;
							break i;
						}
					}

					if (broken == false) {
						points[p] = new Vector(data[0], data[1], data[2]);
					}

					line = in.readLine();
					lineCount++;
					}

					for (int p = 0; p < polyCount; p++) {
						t: while (true) {
							if (line.startsWith("#") == true) {
								line = in.readLine();
								lineCount++;
							} else {
								break t;
							}
						}

					int data[] = new int[3];

					boolean broken = false;

					i: for (int i = 0; i < data.length; i++) {
						int index = line.indexOf(" ");

						if (i == data.length - 1) {
							data[i] = Integer.parseInt(line.trim());

							break i;
						}

						if (index > 0) {
							data[i] = Integer.parseInt(line.substring(0, index).trim());
							line = line.substring(index + 1);
						} else {
							System.out.println("Line " + lineCount + " of RAW file does not contain the proper number of terms, " +
									"or is not properly delimited");

							broken = true;
							break i;
						}
					}

					if (broken == false) {
						ShadableSurface newSurface = null;

						newSurface = new Triangle(points[data[0]], points[data[1]], points[data[2]]);
						((AbstractSurface)newSurface).setColor(new RGB(1.0, 1.0, 1.0));
						((AbstractSurface)newSurface).setShaders(new Shader[0]);

						if (ui == true) {
							System.out.println("FileDecoder: UI mode no longer supported.");
							//								newSurface = SurfaceUIFactory.createSurfaceUI((AbstractSurface)newSurface);
							//								((AbstractSurfaceUI)newSurface).setName("Triangle " + p);
						}

						scene.addSurface(newSurface);
					}

					line = in.readLine();
					lineCount++;
					}

					break l;
				} else {
					try {
						double data[] = new double[9];

						boolean broken = false;

						i: for (int i = 0; i < data.length; i++) {
							int index = line.indexOf(" ");

							if (i == data.length - 1) {
								data[i] = Double.parseDouble(line);

								break i;
							}

							if (index > 0) {
								data[i] = Double.parseDouble(line.substring(0, index));
								line = line.substring(index + 1);
							} else {
								System.out.println("Line " + lineCount + " of RAW file does not contain the proper number of terms, " +
										"or is not properly delimited");

								broken = true;
								break i;
							}
						}

						if (broken == false) {
							Vector p1 = new Vector(data[0], data[1], data[2]);
							Vector p2 = new Vector(data[3], data[4], data[5]);
							Vector p3 = new Vector(data[6], data[7], data[8]);

							ShadableSurface newSurface = null;

							newSurface = new Triangle(p1, p2, p3);
							((AbstractSurface)newSurface).setColor(new RGB(1.0, 1.0, 1.0));
							((AbstractSurface)newSurface).setShaders(new Shader[0]);

							if (ui == true) {
								System.out.println("FileDecoder: UI mode no longer supported.");
								//								newSurface = SurfaceUIFactory.createSurfaceUI((AbstractSurface)newSurface);
								//								((AbstractSurfaceUI)newSurface).setName("Triangle " + tCount);
							}

							scene.addSurface(newSurface);
							tCount++;
						}
					} catch (NumberFormatException nfe) {
						System.out.println("Line " + lineCount + " of RAW file contains nonnumerical data");
					}
				}

				line = in.readLine();
				lineCount++;
			}

			return scene;
		} else if (encoding == GTSEncoding) {
			Mesh m = new Mesh();
			if (s != null && s instanceof Mesh) {
				System.out.println(m);

				m = (Mesh) s;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));

			int lineCount = 0;
			String line = null;

			int pointCount = 0, edgeCount = 0, triangleCount = 0;

			w: while (true) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue w;

				double d[] = FileDecoder.parseDoubles(line);

				pointCount = (int)d[0];
				edgeCount = (int)d[1];
				triangleCount = (int)d[2];

				break w;
			}

			int edges[][] = new int[edgeCount][2];

			i: for (int i = 0; i < pointCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);
				m.addVector(new Vector(d[0], d[1], d[2]));

				i++;
			}

			i: for (int i = 0; i < edgeCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);
				edges[i][0] = (int)d[0] - 1;
				edges[i][1] = (int)d[1] - 1;

				i++;
			}

			i: for (int i = 0; i < triangleCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);

				Stack st = new Stack();

				for (int j = 0; j < d.length; j++) {
					int v[] = edges[(int)d[j] - 1];
					Integer v0 = Integer.valueOf(v[0]);
					Integer v1 = Integer.valueOf(v[1]);

					if (!st.contains(v0)) st.push(v0);
					if (!st.contains(v1)) st.push(v1);
				}

				m.addTriangle(((Integer)st.pop()).intValue(), ((Integer)st.pop()).intValue(), ((Integer)st.pop()).intValue());

				i++;
			}

			if (ui == true) {
				System.out.println("FileDecoder: UI mode no longer supported.");
				//				AbstractSurfaceUI sr[] = {SurfaceUIFactory.createSurfaceUI(m)};
				//				sr[0].setName("Mesh (" + m.getTriangles().length + " Triangles)");
				//				return new Scene(sr);
			}

			return new Scene(new ShadableSurface[] {m});
		} else if (encoding == PLYEncoding) {
			Mesh m = new Mesh();
			if (s != null && s instanceof Mesh) {
				System.out.println(m);
				m = (Mesh) s;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(fileIn));

			int lineCount = 0;
			String line = null;

			int pointCount = 0, triangleCount = 0;

			w: while (true) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;

				if (line.startsWith("element")) {
					if (line.indexOf("vertex") > 0)
						pointCount = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
					if (line.indexOf("face") > 0)
						triangleCount = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
				} else if (line.startsWith("end_header")) {
					break w;
				}
			}

			i: for (int i = 0; i < pointCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);
				m.addVector(new Vector(d[0], d[1], d[2]));

				i++;
			}

			i: for (int i = 0; i < triangleCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);

				m.addTriangle((int) d[1], (int) d[2], (int) d[3]);

				i++;
			}

			if (ui == true) {
				System.out.println("FileDecoder: UI mode no longer supported.");
				//				AbstractSurfaceUI sr[] = {SurfaceUIFactory.createSurfaceUI(m)};
				//				sr[0].setName("Mesh (" + m.getTriangles().length + " Triangles)");
				//				return new Scene(sr);
			}

			return new Scene(new ShadableSurface[] {m});
		} else {
			return null;
		}
	}
}