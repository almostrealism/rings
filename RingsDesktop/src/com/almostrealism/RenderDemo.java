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

package com.almostrealism;

import java.io.File;
import java.io.IOException;

import org.almostrealism.space.Intersection;
import org.almostrealism.space.Ray;
import org.almostrealism.space.TransformMatrix;
import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;
import org.almostrealism.texture.Texture;

import com.almostrealism.projection.ThinLensCamera;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.io.FileEncoder;
import com.almostrealism.raytracer.lighting.StandardLightingRigs;
import com.almostrealism.raytracer.primitives.Plane;
import com.almostrealism.raytracer.primitives.Sphere;
import com.almostrealism.raytracer.primitives.StripeTexture;
import com.almostrealism.raytracer.ui.RenderTestFrame;

/**
 * @author Mike Murray
 */
public class RenderDemo {
	public static final boolean enableCornellBox = false;
	
	public static void main(String[] args) {
		Scene<ShadableSurface> scene = null;
		
		if (enableCornellBox) {
			try {
				scene = FileDecoder.decodeSceneFile(new File("CornellBox.xml"), FileDecoder.XMLEncoding, false, null);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			scene = new Scene<ShadableSurface>();
		}
		
		Sphere s = new Sphere();
		s.setLocation(new Vector(0.0, -0.5, 0.0));
		s.setColor(new RGB(0.8, 0.2, 0.2));
		
		TransformMatrix scale = TransformMatrix.createScaleMatrix(1.0, 1.4, 1.0);
		s.addTransform(scale);
		
		Texture randomTex = new Texture() {
			public RGB getColorAt(Vector point) {
				point.setZ(0.0);
				double d = (point.length() * 4.0) % 3;
				
				if (d < 1) {
					return new RGB (0.5 + Math.random() / 2.0, 0.0, 0.0);
				} else if (d < 2) {
					return new RGB (0.0, 0.5 + Math.random() / 2.0, 0.0);
				} else {
					return new RGB (0.0, 0.0, 0.5 + Math.random() / 2.0);
				}
			}

			public RGB getColorAt(Vector point, Object args[]) { return this.getColorAt(point); }
			public RGB evaluate(Object args[]) { return this.getColorAt((Vector) args[0]); }
		};
		
		s.addTexture(randomTex);
		
		AbstractSurface thing = new AbstractSurface() {
			private Plane p = new Plane(Plane.XY);
			
			public Vector getNormalAt(Vector point) { return new Vector(0.0, 0.0, 1.0); }
			
			public boolean intersect(Ray ray) {
				ray.transform(this.getTransform(true).getInverse());
				return this.p.intersect(ray);
			}
			
			public Intersection intersectAt(Ray ray) {
				ray.transform(this.getTransform(true).getInverse());
				
				if (Math.random() > 0.5) {
					return this.p.intersectAt(ray);
				} else {
					return new Intersection(ray, this, new double[0]);
				}
			}
		};
		
		thing.setLocation(new Vector(0.0, 0.0, 5.0));
		thing.setColor(new RGB(1.0, 1.0, 1.0));
		
		StripeTexture stripes = new StripeTexture();
		stripes.setAxis(StripeTexture.XAxis);
		stripes.setStripeWidth(0.25);
		stripes.setFirstColor(new RGB(1.0, 0.0, 0.0));
		stripes.setSecondColor(new RGB(0.0, 0.0, 1.0));
		thing.addTexture(stripes);
		
		scene.add(s);
		scene.add(thing);
		
		StandardLightingRigs.addDefaultLights(scene);
		
		ThinLensCamera c = new ThinLensCamera();
		c.setLocation(new Vector(0.0, 0.0, 10.0));
		c.setViewDirection(new Vector(0.0, 0.0, -1.0));
		c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
		c.setFocalLength(0.05);
		c.setFocus(10.0);
		c.setLensRadius(0.2);
		
		scene.setCamera(c);
		
		try {
			FileEncoder.encodeSceneFile(scene, new File("RenderDemo.xml"), FileEncoder.XMLEncoding);
			// scene = FileDecoder.decodeSceneFile(new File("RenderDemo.xml"), FileDecoder.XMLEncoding, false, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		RenderTestFrame f = new RenderTestFrame(scene, 200, 2);
		f.render();
	}
}
