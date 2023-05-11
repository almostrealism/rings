/*
 * Copyright 2022 Michael Murray
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

package com.almostrealism.raytracer;

import java.util.Arrays;
import java.util.List;

import org.almostrealism.algebra.Vector;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.RandomColorGenerator;
import org.almostrealism.geometry.Positioned;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.RandomChromosomeFactory;
import org.almostrealism.heredity.ProbabilisticFactory;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import io.almostrealism.relation.Factory;

import com.almostrealism.lighting.PointLight;
import org.almostrealism.projection.OrthographicCamera;
import com.almostrealism.projection.ThinLensCamera;
import org.almostrealism.primitives.Sphere;

/**
 * The {@link SceneFactory} class provides static utility methods for getting commonly
 * used components of a {@link Scene} for the ray tracing engine as well as a method
 * for generating a {@link Scene} from a {@link Chromosome}.
 * 
 * @author  Michael Murray
 */
public class SceneFactory implements Factory<Scene<ShadableSurface>> {
	private ProbabilisticFactory<ShadableSurface> surfaces;
	
	public SceneFactory() {
		this(new RandomChromosomeFactory().setChromosomeSize(1, 1).generateChromosome(1.0));
	}
	
	public SceneFactory(Chromosome<PackedCollection<?>> c) {
		List<Factory<ShadableSurface>> f =
			Arrays.asList(
					() -> new Sphere(location(), Math.random(), color())
			);
		
		surfaces = new ProbabilisticFactory<>(f, c.valueAt(0));
	}
	
	private int count() { return (int) (2 + Math.random() * 4); }
	
	private Vector location() {
		return new Vector((Math.random() * 10) - 5,
							(Math.random() * 5),
							(Math.random() * 10) - 5);
	}
	
	/** TODO  Use {@link RandomColorGenerator}. */
	private RGB color() { return new RGB(Math.random(), Math.random(), Math.random()); }
	
	/**
	 * @see io.almostrealism.relation.Factory#construct()
	 */
	@Override
	public Scene<ShadableSurface> construct() {
		Scene<ShadableSurface> s = new Scene<ShadableSurface>();
		s.setLights(getStandard3PointLightRig(10));
		s.setCamera(new ThinLensCamera());
		((Positioned) s.getCamera()).setPosition(0, 5, 10);
		((OrthographicCamera) s.getCamera()).setViewDirection(new Vector(0.0, -0.75, -1.0));
		
		int c = count();
		
		for (int i = 0; i < c; i++) s.add(surfaces.construct());
		return s;
	}
	
	public static List<Light> getStandard3PointLightRig(double scale) {
		Light l[] = new Light[3];
		
		l[0] = new PointLight(new Vector(scale, scale, scale));
		l[1] = new PointLight(new Vector(-scale, scale, scale));
		l[2] = new PointLight(new Vector(0.0, scale, -scale));
		
		return Arrays.asList(l);
	}
}
