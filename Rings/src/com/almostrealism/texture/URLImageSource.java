package com.almostrealism.texture;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.net.URL;

import javax.swing.ImageIcon;

public class URLImageSource implements ImageSource {
	private URL url;
	
	private Image image;
	private int pixels[];

	public URLImageSource(URL url) {
		this.url = url;

		Image image = Toolkit.getDefaultToolkit().getImage(this.url);
		MediaTracker m = new MediaTracker(new Panel());
		m.addImage(image, 0);

		try {
			m.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("ImageTexture: Wait for image loading was interrupted.");
		}

		if (m.isErrorAny()) throw new RuntimeException("ImageTexture: Error loading image.");
	}

	@Override
	public int[] getPixels() {
		if (pixels != null) return pixels;
		
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		this.pixels = new int[width * height];

		PixelGrabber p = new PixelGrabber(image, 0, 0, width, height, this.pixels, 0, width);

		try {
			p.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("ImageTexture: Pixel grabbing interrupted.");
		}
		
		return pixels;
	}

	@Override
	public int getWidth() { return image.getWidth(null); }

	@Override
	public int getHeight() { return image.getHeight(null); }
	
	@Override
	public boolean isAlpha() { return false; }
	
	public ImageIcon getIcon() { return new ImageIcon(url); }
}
