/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package net.sf.j3d.ui.displays;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;

import javax.swing.JPanel;

import com.almostrealism.io.FileEncoder;
import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;


/**
 * An ImageCanvas object stores image data and paints its parent class (JPanel)
 * using the image.
 * 
 * @author Mike Murray
 */
public class ImageCanvas extends JPanel {
  private int screenX, screenY;
  private double xScale, yScale;
  private double xOff, yOff;
  
  private RGB image[][];
  private RGB color;
  private int next;

  	
  
	public ImageCanvas(int w, int h) {
		this(w, h, 1.0, 1.0, 0.0, 0.0);
	}
	
	/**
	 * Constructs a new ImageCanvas object.
	 * 
	 * @param w  Image width.
	 * @param h  Image height.
	 * @param xScale  X scale factor.
	 * @param yScale  Y scale factor.
	 * @param xOff  X offset.
	 * @param yOff  Y offset.
	 */
	public ImageCanvas(int w, int h, double xScale, double yScale, double xOff, double yOff) {
		this.image = new RGB[w][h];
		this.color = new RGB(0.0, 0.0, 0.0);
		
		this.screenX = w;
		this.screenY = h;
		this.xScale = xScale;
		this.yScale = yScale;
		this.xOff = xOff;
		this.yOff = yOff;
		
		this.clear();
	}
	
	public void setXScale(double xScale) { this.xScale = xScale; }
	
	public void setYScale(double yScale) { this.yScale = yScale; }
	
	public void setXOffset(double xOff) { this.xOff = xOff; }
	
	public void setYOffset(double yOff) { this.yOff = yOff; }
	
	public double getXScale() { return this.xScale; }
	
	public double getYScale() { return this.yScale; }
	
	public double getXOffset() { return this.xOff; }
	
	public double getYOffset() { return this.yOff; }
	
	/**
	 * Plots a point on this ImageCanvas object.
	 * 
	 * @param x  X coordinate.
	 * @param y  Y coordinate.
	 * @param c  Color to use for point.
	 */
	public void plot(double x, double y, RGB c) {
		int sx = (int)(((x + this.xOff) * this.xScale) + (this.screenX / 2.0));
		int sy = (int)(-((y + this.yOff) * this.yScale) + (this.screenY / 2.0));
		
		this.next++;
		
		if (sx >= 0 && sx < this.image.length && sy >= 0 && sy < this.image[sx].length) {
			this.image[sx][sy] = c;
			this.color = this.image[sx][sy];
		}
		
		this.repaint();
	}
	
	/**
	 * Sets the color at a pixel in the image data stored by this ImageCanvas object.
	 * 
	 * @param i  Index into image array.
	 * @param j  Index into image array.
	 * @param rgb  RGB object to use for pixel color.
	 */
	public void setImageData(int i, int j, RGB rgb) { this.image[i][j] = rgb; }
	
	/**
	 * Sets the image data stored by this ImageCanvas object.
	 * 
	 * @param image  RGB array to use for image data.
	 */
	public void setImageData(RGB image[][]) { this.image = image; }
	
	/**
	 * @return  The image data stored by this ImageCanvas object.
	 */
	public RGB[][] getImageData() { return this.image; }
	
	/**
	 * Clears this ImageCanvas object.
	 */
	public void clear() {
		for (int i = 0; i < this.image.length; i++) {
			for (int j = 0; j < this.image[i].length; j++) {
				this.image[i][j] = new RGB(0.0, 0.0, 0.0);
			}
		}
	}
	
	/**
	 * Writes the image data stored by this ImageCanvas object out to the specified file.
	 * 
	 * @param file  File name.
	 */
	public void writeImage(String file) {
		try {
			FileEncoder.encodeImageFile(this.image,
							new File(file),
							FileEncoder.JPEGEncoding);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Overrides normal JPanel paint method.
	 */
	public void paint(Graphics g) {
		Image img = GraphicsConverter.convertToAWTImage(this.image);
		g.drawImage(img, 0, 0, Color.black, this);
	}
}