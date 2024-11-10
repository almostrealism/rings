/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.network;

import com.almostrealism.lighting.StandardLightingRigs;
import org.almostrealism.projection.OrthographicCamera;
import org.almostrealism.primitives.Sphere;
import com.almostrealism.projection.ThinLensCamera;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.SilhouetteShader;
import com.almostrealism.raytracer.Thing;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBFeatures;
import org.almostrealism.color.Shader;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.space.DefaultVertexData;
import org.almostrealism.space.Mesh;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.StripeTexture;
import org.almostrealism.texture.Texture;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Evaluable;

import java.io.FileInputStream;
import java.io.IOException;

public class TestScene extends Scene<ShadableSurface> implements RGBFeatures, CodeFeatures {
	public TestScene() throws IOException {
		this(false, false, false, true, false, false, false, false, false, false);
	}

	public TestScene(boolean enableCornellBox, boolean enableRandomThing, boolean enableDragon,
					 boolean enableSphere, boolean enableTriangles, boolean enableFloor,
					 boolean enableSilhouette,
					 boolean enableSphereReflection, boolean enableFloorReflection,
					 boolean enableThinLensCamera) throws IOException {
		if (enableCornellBox) {
			addAll(FileDecoder.decodeScene(new FileInputStream("CornellBox.xml"),
						FileDecoder.XMLEncoding, false, null));
		}

		Sphere s = new Sphere();

		if (enableSphere) {
			if (enableSilhouette) {
				s.setShaders(new Shader[] { new SilhouetteShader(white()) });
			} else if (enableSphereReflection) {
				s.setShaders(new Shader[] {
						new ReflectionShader(0.6, rgb(0.6))
				});
			} else {
				s.setShaders(new Shader[] { DiffuseShader.defaultDiffuseShader });
			}

			s.setLocation(new Vector(0.0, 0.0, -3.0));
			s.setColor(new RGB(0.8, 0.8, 0.8));
			add(s);

//			if (enableSilhouette) {
//				Sphere s2 = new Sphere();
//				s2.setShaders(new Shader[]{new SilhouetteShader(white())});
//				s2.setLocation(new Vector(0.0, 2.4, -3.0));
//				s2.setColor(new RGB(0.8, 0.8, 0.8));
//				s2.setSize(0.25);
//				add(s2);
//
//				Sphere s3 = new Sphere();
//				s3.setShaders(new Shader[]{new SilhouetteShader(white())});
//				s3.setLocation(new Vector(0.0, 1.4, -3.0));
//				s3.setColor(new RGB(0.8, 0.8, 0.8));
//				s3.setSize(0.25);
//				add(s3);
//
//				Sphere s4 = new Sphere();
//				s4.setShaders(new Shader[]{new SilhouetteShader(white())});
//				s4.setLocation(new Vector(0.0, 0.4, -3.0));
//				s4.setColor(new RGB(0.8, 0.8, 0.8));
//				s4.setSize(0.25);
//				add(s4);
//			}
		}

		if (enableFloor) {
			Plane p = new Plane(Plane.XZ);
			p.setTextures(new Texture[] { new StripeTexture() });

			if (enableFloorReflection) {
				p.setShaders(new Shader[] {
						new ReflectionShader(0.65, white())
				});
			}

			p.setShaders(new Shader[] { DiffuseShader.defaultDiffuseShader });

			p.setLocation(new Vector(0.0, -1.0, 0.0));
			p.setColor(new RGB(0.5, 0.5, 0.7));
			add(p);
		}

		if (enableRandomThing) {
			Texture randomTex = new Texture() {
				Producer<RGB> p = GeneratedColorProducer.fromProducer(this, () -> args -> {
					Vector t = args.length > 0 ? (Vector) args[0] : new Vector(1.0, 1.0, 1.0);
					Vector point = new Vector(t.getX(), t.getY(), 0.0);
					double d = (point.length() * 4.0) % 3;

					if (d < 1) {
						return new RGB (0.5 + Math.random() / 2.0, 0.0, 0.0);
					} else if (d < 2) {
						return new RGB (0.0, 0.5 + Math.random() / 2.0, 0.0);
					} else {
						return new RGB (0.0, 0.0, 0.5 + Math.random() / 2.0);
					}
				});

				public Evaluable<RGB> getColorAt(Object args[]) { return p.get(); }

				@Override
				public RGB operate(Vector in) { return p.get().evaluate(in); }
			};

			s.addTexture(randomTex);

			AbstractSurface thing = new Thing();

			thing.setLocation(new Vector(0.0, 0.0, 5.0));
			thing.setColor(new RGB(1.0, 1.0, 1.0));

			StripeTexture stripes = new StripeTexture();
			stripes.setAxis(StripeTexture.XAxis);
			stripes.setStripeWidth(0.25);
			stripes.setFirstColor(new RGB(1.0, 0.0, 0.0));
			stripes.setSecondColor(new RGB(0.0, 0.0, 1.0));
			thing.addTexture(stripes);

			add(thing);
		}

		if (enableTriangles) {
			DefaultVertexData data = new DefaultVertexData(5, 3);
			data.getVertices().set(0, new Vector(0.0, 1.0, 0.0));
			data.getVertices().set(1, new Vector(-1.0, -1.0, 0.0));
			data.getVertices().set(2, new Vector(1.0, -1.0, 0.0));
			data.getVertices().set(3, new Vector(-1.0, 1.0, -1.0));
			data.getVertices().set(4, new Vector(1.0, 1.0, -1.0));
			data.setTriangle(0, 0, 1, 2);
			data.setTriangle(1, 3, 1, 0);
			data.setTriangle(2, 0, 2, 4);

			Mesh triangle = new Mesh(data);
			triangle.setShaders(new Shader[] { DiffuseShader.defaultDiffuseShader });
			triangle.setColor(new RGB(0.3, 0.4, 0.9));
			add(triangle);
		}

		if (enableDragon) {
			Mesh dragon = (Mesh) ((Scene<ShadableSurface>) FileDecoder.decodeScene(getClass().getClassLoader().getResourceAsStream("dragon.ply"),
					FileDecoder.PLYEncoding, false, null)).get(0);

			if (enableSilhouette) {
				dragon.setShaders(new Shader[] { new SilhouetteShader(white()) });
			} else {
				dragon.setShaders(new Shader[] { DiffuseShader.defaultDiffuseShader });
			}

			dragon.setColor(new RGB(0.3, 0.4, 0.8));
			dragon.setLocation(new Vector(0.0, -2.4, 0.0));
			dragon.setSize(25); // TODO  Size does not appear to be taking effect
			add(dragon);
		}

		StandardLightingRigs.addDefaultLights(this);

		if (enableThinLensCamera) {
			ThinLensCamera c = new ThinLensCamera();
			c.setLocation(new Vector(0.0, 0.0, 10.0));
			c.setViewDirection(new Vector(0.0, 0.0, -1.0));
			c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
			c.setFocalLength(0.05);
			c.setFocus(10.0);
			c.setLensRadius(0.2);
			setCamera(c);
		} else {
			OrthographicCamera c = new OrthographicCamera();
			c.setLocation(new Vector(0.0, 0.0, 10.0));
			c.setViewDirection(new Vector(0.0, 0.0, -1.0));
			c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
			setCamera(c);
		}
	}
}
