package com.almostrealism.raytracer.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.almostrealism.color.ColorProducer;
import org.almostrealism.space.Vector;

public class RayTracer {
	private final ExecutorService pool = Executors.newFixedThreadPool(10);
	
	private Engine engine;
	
	public RayTracer(Engine e) {
		this.engine = e;
	}
	
	public Future<ColorProducer> trace(Vector from, Vector direction) {
		return pool.submit(() -> {
			return engine.trace(from, direction);
		});
	}
	
	public static interface Engine {
		public ColorProducer trace(Vector from, Vector direction);
	}
}
