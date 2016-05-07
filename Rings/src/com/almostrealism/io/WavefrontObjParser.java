package com.almostrealism.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.almostrealism.raytracer.primitives.ObjPolygon;

/**
 * The {@link WavefrontObjParser} is used to parse 3d objects stored in
 * the Wavefront OBJ file format. Not all features of the file format are
 * supported.
 * 
 * @author  Michael Murray
 */
public class WavefrontObjParser {
	private BufferedReader reader;
	private ArrayList vertices, texCoords, faces;
	
	public WavefrontObjParser(InputStream in) {
		this.reader = new BufferedReader(new InputStreamReader(in));
		this.vertices = new ArrayList();
		this.texCoords = new ArrayList();
		this.faces = new ArrayList();
	}
	
	public void parse() throws IOException {
		w: while (true) {
			String line = this.reader.readLine();
			if (line == null) break w;
			
			if (line.startsWith("v ")) {
				String s[] = line.split(" ");
				float f[] = new float[3];
				f[0] = Float.parseFloat(s[1]);
				f[1] = Float.parseFloat(s[2]);
				f[2] = Float.parseFloat(s[3]);
				this.vertices.add(f);
			} else if (line.startsWith("vt ")) {
				String s[] = line.split(" ");
				float f[] = new float[3];
				f[0] = Float.parseFloat(s[1]);
				f[1] = Float.parseFloat(s[2]);
				this.texCoords.add(f);
			} else if (line.startsWith("f ")) {
				String s[] = line.split(" ");
				ArrayList faceVerts = new ArrayList();
				ArrayList faceTexCoords = new ArrayList();
				
				for (int i = 1; i < s.length; i++) {
					String l[] = s[i].split("/");
					float vertex[] = (float[]) this.vertices.get(Integer.parseInt(l[0]) - 1);
					faceVerts.add(vertex);
					
					if (l.length > 1 && l[1].length() > 0) {
						float texCoord[] = (float[]) this.texCoords.get(Integer.parseInt(l[1]) - 1);
						faceTexCoords.add(texCoord);
					} else {
						float texCoord[] = new float[] {0.0f, 0.0f};
						faceTexCoords.add(texCoord);
					}
				}
				
				ObjPolygon face = new ObjPolygon();
				face.setVertices((float[][]) faceVerts.toArray(new float[0][0]));
				face.setTexCoords((float[][]) faceTexCoords.toArray(new float[0][0]));
				this.faces.add(face);
			}
		}
	}
	
	public ObjPolygon[] getFaces() {
		return (ObjPolygon[]) this.faces.toArray(new ObjPolygon[0]);
	}
}
