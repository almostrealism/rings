package com.almostrealism.raytracer.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;

public class RayTracer {
	private static final ExecutorService pool = Executors.newFixedThreadPool(10);
	
	private Engine engine;
	
	public RayTracer(Engine e) {
		this.engine = e;
	}
	
	public Future<ColorProducer> trace(Vector from, Vector direction) {
		return pool.submit(() -> engine.trace(from, direction));
	}
	
	public interface Engine {
		ColorProducer trace(Vector from, Vector direction);
	}
	
	public static ExecutorService getExecutorService() { return pool; }
}
