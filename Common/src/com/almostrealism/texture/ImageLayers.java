package com.almostrealism.texture;

import java.util.HashMap;
import java.util.Map;

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
	
	public void addLayers(ImageLayers l) {
		for (Map.Entry<String, ImageSource> m : l.layers.entrySet()) {
			layers.put(m.getKey(), m.getValue());
		}
	}
	
	public void clear() { layers.clear(); }

	@Override
	public int[] getPixels() { return layers.values().iterator().next().getPixels(); }

	@Override
	public int getWidth() { return layers.values().iterator().next().getWidth(); }

	@Override
	public int getHeight() { return layers.values().iterator().next().getHeight(); }

	@Override
	public boolean isAlpha() { return layers.values().iterator().next().isAlpha(); }
}
