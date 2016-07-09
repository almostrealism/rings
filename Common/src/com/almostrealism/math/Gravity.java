package com.almostrealism.math;
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

import javax.swing.JFrame;

import com.almostrealism.ui.displays.ImageCanvas;
import com.almostrealism.util.graphics.RGB;


/**
 * @author Mike Murray
 */
public class Gravity {
	public static void main(String[] args) {
		int w = 200, h = 200;
		
		ImageCanvas canvas = new ImageCanvas(w, h, 1.0, 1.0, 0.0, 0.0);
		
		canvas.plot(0.0, 0.0, new RGB(0.0, 0.0, 1.0));
		
//		for (int i = 0; i < 5; i++) {
//			int x = (int)(Math.random() * (w - 3)) + 1;
//			int y = (int)(Math.random() * (h - 3)) + 1;
//			
//			canvas.setImageData(x, y, new RGB(1.0, 1.0, 1.0));
//		}
		
		JFrame frame = new JFrame("Gravity");
		frame.setSize(w, h);
		frame.getContentPane().add(canvas);
		frame.setVisible(true);
		
		RGB black = new RGB(1.0, 1.0, 1.0);
		RGB blue = new RGB(0.0, 0.0, 1.0);
		
		RGB image[][] = canvas.getImageData();
		double field[][] = new double[image.length][image[0].length];
		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
//				field[i][j] = Math.random();
				
				for (int k = 0; k < image.length; k++) {
					for (int l = 0; l < image[k].length; l++) {
						if (image[k][l].equals(black)) {
							double ik = i - k;
							double jl = j - l;
							double r = ik * ik + jl * jl;
							
							field[i][j] = field[i][j] + 1.0 / r;
						}
					}
				}
			}
		}
		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				image[i][j] = new RGB(field[i][j], field[i][j], field[i][j]);
			}
		}
		
		canvas.setImageData(image);
		
		System.out.println("Done initializing field");
		
		for (int l = 0; l < 2000; l++) {
			int x = (int)(Math.random() * (w - 3)) + 1;
			int y = (int)(Math.random() * (h - 3)) + 1;
			
			System.out.println(l);
			
			// System.out.println(x + " " + y);
			
			w: while (true) {
				if (image[x - 1][y - 1].equals(blue) ||
					image[x - 1][y].equals(blue) ||
					image[x - 1][y + 1].equals(blue) ||
					image[x][y - 1].equals(blue) ||
					image[x][y + 1].equals(blue) ||
					image[x + 1][y - 1].equals(blue) ||
					image[x + 1][y].equals(blue) ||
					image[x + 1][y + 1].equals(blue)) {
					break w;
				}
				
				double f = field[x - 1][y - 1];
				int newX = x - 1, newY = y - 1;
				
				if (field[x - 1][y] > f) {
					f = field[x - 1][y];
					newX = x - 1;
					newY = y;
				}
				
				if (field[x - 1][y + 1] > f) {
					f = field[x - 1][y + 1];
					newX = x - 1;
					newY = y + 1;
				}
				
				if (field[x][y - 1] > f) {
					f = field[x][y - 1];
					newX = x;
					newY = y - 1;
				}
				
				if (field[x][y + 1] > f) {
					f = field[x][y + 1];
					newX = x;
					newY = y + 1;
				}
				
				if (field[x + 1][y - 1] > f) {
					f = field[x + 1][y - 1];
					newX = x + 1;
					newY = y - 1;
				}
				
				if (field[x + 1][y] > f) {
					f = field[x + 1][y];
					newX = x + 1;
					newY = y;
				}
				
				if (field[x + 1][y + 1] > f) {
					f = field[x + 1][y + 1];
					newX = x + 1;
					newY = y + 1;
				}
				
				x = newX;
				y = newY;
			}
			
			// System.out.println(x + " " + y);
			
			image = canvas.getImageData();
			
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[i].length; j++) {
					double dx = i - x;
					double dy = j - y;
					double r = dx * dx + dy * dy;
					
					field[i][j] = field[i][j] + 1.0 / r;
					
					if (!image[i][j].equals(blue)) image[i][j] = new RGB(field[i][j], field[i][j], field[i][j]);
				}
			}
			
			canvas.setImageData(image);
			canvas.setImageData(x, y, blue);
			canvas.repaint();
		}
		
		canvas.writeImage("Gravity-Field-Fractal.jpg");
	}
}
