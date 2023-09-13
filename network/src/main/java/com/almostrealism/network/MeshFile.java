/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.network;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.ContinuousField;
import io.almostrealism.relation.Producer;
import io.almostrealism.code.Operator;
import org.almostrealism.space.BoundingSolid;
import org.almostrealism.space.Mesh;
import org.almostrealism.space.MeshSource;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.space.ShadableSurfaceWrapper;

public class MeshFile implements MeshSource, ShadableSurfaceWrapper, ShadableSurface {
	private String name;
	private int format;
	private String url;
	private Mesh mesh;
	private ShadableSurface s;

	public void setFile(String f) { this.name = f; }
	public String getFile() { return this.name; }
	public void setFormat(int f) { this.format = f; }
	public int getFormat() { return this.format; }
	public void setURL(String url) { this.url = url; }
	public String getURL() { return this.url; }

	public void setSurface(ShadableSurface s) { this.s = s; }

	public ShadableSurface getSurface() {
		if (this.mesh == null) {
			try {
				if (this.url != null) {
					URL url = new URL(this.url + this.name);

					this.mesh = (Mesh) ModelData.decodeScene(url.openStream(),
							this.format, false, null, this.s).getSurfaces()[0];
				} else {
					this.mesh = (Mesh) FileDecoder.decodeSurfaceFile(
							new File(this.name), this.format, false, null, this.s);
				}
			} catch (IOException ioe) {
				System.out.println("Mesh.MeshFile: IO error loading mesh data - " +
						ioe.getMessage());
			}

			if (this.mesh == null) {
				System.out.println("Mesh.MeshFile: Unexpected failure loading mesh data.");
				return null;
			}

			this.mesh.setMeshSource(this);
			this.mesh.loadTree();
		}

		return this.mesh;
	}

	@Override public boolean getShadeFront() { return this.getSurface().getShadeFront(); }
	@Override public boolean getShadeBack() { return this.getSurface().getShadeBack(); }
	@Override public Producer<RGB> getValueAt(Producer<Vector> point) { return this.getSurface().getValueAt(point); }

	@Override
	public BoundingSolid calculateBoundingSolid() { return mesh.calculateBoundingSolid(); }

	@Override public Producer<Vector> getNormalAt(Producer<Vector> point) { return this.getSurface().getNormalAt(point); }
	@Override public ContinuousField intersectAt(Producer<Ray> ray) { return this.getSurface().intersectAt(ray); }

	@Override public Operator<Scalar> get() { return getSurface().get(); }

	@Override
	public Operator<Scalar> expect() { return getSurface().expect(); }

	@Override public Producer<RGB> shade(ShaderContext p) { return this.getSurface().shade(p); }

	@Override public RGB operate(Vector in) { return getSurface().operate(in); }
}
