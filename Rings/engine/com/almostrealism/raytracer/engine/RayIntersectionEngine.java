package com.almostrealism.raytracer.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Intersectable;
import org.almostrealism.space.Intersection;

import com.almostrealism.lighting.AmbientLight;
import com.almostrealism.lighting.DirectionalAmbientLight;
import com.almostrealism.lighting.Light;
import com.almostrealism.lighting.PointLight;
import com.almostrealism.lighting.SurfaceLight;
import com.almostrealism.projection.Intersections;
import com.almostrealism.rayshade.ShadableIntersection;
import com.almostrealism.rayshade.ShaderParameters;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.Settings;

public class RayIntersectionEngine implements RayTracer.Engine {
	private Scene<ShadableSurface> scene;
	private RenderParameters rparams;
	
	public RayIntersectionEngine(Scene<ShadableSurface> s, RenderParameters rparams) {
		this.scene = s;
		this.rparams = rparams;
	}
	
	public ColorProducer trace(Vector from, Vector direction) {
		Ray r = new Ray(from, direction);
		
		return LightingEngine.lightingCalculation(r, scene, scene.getLights(), rparams.fogColor,
									rparams.fogDensity, rparams.fogRatio, null);
	}
}
