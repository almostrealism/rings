package com.almostrealism.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import io.almostrealism.resource.ResourceTranscoder;
import io.almostrealism.resource.UnicodeResource;
import org.almostrealism.space.DefaultVertexData;
import org.almostrealism.space.Mesh;

public class PlyResource extends UnicodeResource {
	public static boolean enableVerbose = false;

	public PlyResource() { }

	public PlyResource(File f) throws IOException { super(f); }

	public static class MeshTranscoder implements ResourceTranscoder<MeshResource, PlyResource> {
		@Override
		public PlyResource transcode(MeshResource r) {
			return null;
		}
	}

	public static class MeshReader implements ResourceTranscoder<PlyResource, MeshResource> {
		public void setInitialMesh(Mesh m) {
			throw new RuntimeException("setInitialMesh no longer supported");
		}

		@Override
		public MeshResource transcode(PlyResource r) throws IOException {
			BufferedReader in = new BufferedReader(new InputStreamReader(r.getInputStream()));

			int lineCount = 0;
			String line;

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

			DefaultVertexData data = new DefaultVertexData(pointCount, triangleCount);

			i: for (int i = 0; i < pointCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);
				data.getVertices().set(i, d[0], d[1], d[2]);

				i++;

				if (enableVerbose && i % 100000 == 0) {
					System.out.println("PlyResource: " + i + " of " + pointCount + " points loaded");
				}
			}

			System.out.println("PlyResource: " + pointCount + " points loaded");

			i: for (int i = 0; i < triangleCount; ) {
				line = in.readLine();
				lineCount++;
				if (line == null) return null;
				if (line.startsWith("#")) continue i;

				double d[] = FileDecoder.parseDoubles(line);
				data.setTriangle(i, (int) d[1], (int) d[2], (int) d[3]);

				i++;

				if (enableVerbose && i % 100000 == 0) {
					System.out.println("PlyResource: " + i + " of " + triangleCount + " triangles loaded");
				}
			}

			System.out.println("PlyResource: " + triangleCount + " triangles loaded");

			return new MeshResource(new Mesh(data));
		}
	}
}
