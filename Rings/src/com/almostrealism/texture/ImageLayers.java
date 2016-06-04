package com.almostrealism.texture;

import java.util.HashMap;

/**
 * TODO  Add support for blending layers
 * 
 * @author  Michael Murray
 */
public class ImageLayers implements ImageSource {
	private HashMap<String, ImageSource> layers;
	
	public ImageLayers() {
		this.layers = new HashMap<String, ImageSource>();
	}
	
	public void addLayer(String name, ImageSource image) {
		layers.put(name, image);
	}
	
	public ImageSource getLayer(String name) { return layers.get(name); }

	@Override
	public int[] getPixels() { return layers.values().iterator().next().getPixels(); }

	@Override
	public int getWidth() { return layers.values().iterator().next().getWidth(); }

	@Override
	public int getHeight() { return layers.values().iterator().next().getHeight(); }

	@Override
	public boolean isAlpha() { return layers.values().iterator().next().isAlpha(); }
}
