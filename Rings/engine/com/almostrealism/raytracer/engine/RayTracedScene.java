package com.almostrealism.raytracer.engine;

import java.util.concurrent.ExecutionException;

import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.space.Ray;

import com.almostrealism.projection.Camera;
import com.almostrealism.raytracer.Scene;

import io.almostrealism.lambda.Realization;

public class RayTracedScene implements Realization<RealizableImage, RenderParameters> {
	private RayTracer tracer;
	private Camera camera;
	
	public RayTracedScene(RayTracer.Engine t, Camera c) {
		this.tracer = new RayTracer(t);
		this.camera = c;
	}
	
	@Override
	public RealizableImage realize(RenderParameters p) {
		ColorProducer image[][] = new ColorProducer[p.dx][p.dy];
		
		for (int i = p.x; i < (p.x + p.dx); i++) {
			for (int j = p.y; j < (p.y + p.dy); j++) {
				for (int k = 0; k < p.ssWidth; k++)
				l: for (int l = 0; l < p.ssHeight; l++) {
					double r = i + ((double) k / (double) p.ssWidth);
					double q = j + ((double) l / (double) p.ssHeight);
					
					Ray ray = camera.rayAt(r, p.height - q, p.width, p.height);
//					RGB color = LegacyRayTracingEngine.lightingCalculation(ray, data, data.getLights(),
//										p.fogColor, p.fogDensity, p.fogRatio, null).evaluate(null);
					
					ColorProducer color = null;
					
					try {
						color = tracer.trace(ray.getOrigin(), ray.getDirection()).get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
						continue l;
					}
					
					if (color == null) {
						color = LegacyRayTracingEngine.black;
					}
					
					if (image[i - p.x][j - p.y] == null) {
						if (p.ssWidth > 1 || p.ssHeight > 1) {
							double scale = 1.0 / (p.ssWidth * p.ssHeight);
							color = new ColorProduct(color, new RGB(scale, scale, scale));
						}
						
						image[i - p.x][j - p.y] = color;
					} else {
						double scale = 1.0 / (p.ssWidth * p.ssHeight);
						color = new ColorProduct(color, new RGB(scale, scale, scale));
						
						image[i - p.x][j - p.y] = new ColorSum(image[i - p.x][j - p.y], color);
					}
				}
			}
		}
		
		return new RealizableImage(image);
	}
}
