/*
 * Copyright 2016 Michael Murray
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

package org.almostrealism.math;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.almostrealism.color.RGB;
import org.almostrealism.swing.panels.PercentagePanel;
import org.almostrealism.texture.ImageCanvas;
import org.nfunk.jep.JEP;


/**
 * @author Mike Murray
 */
public class RandomFractal extends JPanel {
  private static int iterations = -1;
  private static int screenX = 1280, screenY = 854;
  private static double xScale = 18000, yScale = 12000;
  private static double xOff = -0.03, yOff = -0.03;
  private static String expFile = "exp.conf";
  private static String xVar = "x", yVar = "y", rVar = "r";
  private static RGB color = new RGB(1.0, 1.0, 1.0);
  private static JEP xParser = new JEP();
  private static JEP yParser = new JEP();

	public static void main(String[] args) {
		if (args.length > 0) RandomFractal.iterations = Integer.parseInt(args[0]);
		if (args.length > 1) RandomFractal.screenX = Integer.parseInt(args[1]);
		if (args.length > 2) RandomFractal.screenY = Integer.parseInt(args[2]);
		if (args.length > 3) RandomFractal.xScale = Double.parseDouble(args[3]);
		if (args.length > 4) RandomFractal.yScale = Double.parseDouble(args[4]);
		if (args.length > 5) RandomFractal.xOff = Double.parseDouble(args[5]);
		if (args.length > 6) RandomFractal.yOff = Double.parseDouble(args[6]);
		
		double lastX = 0.0, lastY = 0.0;
		
		try {
			FileReader efile = new FileReader(RandomFractal.expFile);
			BufferedReader ereader = new BufferedReader(efile);
			lastX = Double.parseDouble(ereader.readLine());
			lastY = Double.parseDouble(ereader.readLine());
			String xExp = ereader.readLine();
			String yExp = ereader.readLine();
			RandomFractal.loadParsers(xExp, yExp);
		} catch (FileNotFoundException fnf) {
			System.out.println("File not found: " + RandomFractal.expFile);
			System.exit(1);
		} catch (IOException ioe) {
			System.out.println("IO Error reading: " + RandomFractal.expFile);
			System.exit(2);
		}
		
		final ImageCanvas canvas = new ImageCanvas(RandomFractal.screenX, RandomFractal.screenY,
													RandomFractal.xScale, RandomFractal.yScale,
													RandomFractal.xOff, RandomFractal.yOff);
		
		System.out.println("Initialized Canvas");
		
		PercentagePanel lp = null;
		JSlider scaleSlider = null, xOffsetSlider = null, yOffsetSlider = null;
		
		if (args.length <= 0) {
			lp = new PercentagePanel();
			
			scaleSlider = new JSlider(JSlider.VERTICAL, 0, 10000, 1);
			xOffsetSlider = new JSlider(JSlider.HORIZONTAL, 0, 2000, 500);
			yOffsetSlider = new JSlider(JSlider.VERTICAL, 0, 2000, 500);
			
			scaleSlider.setMajorTickSpacing(1000);
			scaleSlider.setMinorTickSpacing(250);
			xOffsetSlider.setMajorTickSpacing(100);
			yOffsetSlider.setMajorTickSpacing(100);
			
			scaleSlider.setPaintTicks(true);
			xOffsetSlider.setPaintTicks(true);
			yOffsetSlider.setPaintTicks(true);
			
			scaleSlider.setPaintTrack(false);
			xOffsetSlider.setPaintTrack(false);
			yOffsetSlider.setPaintTrack(false);
			
			lp.setValue(0.8);
			
			JMenuBar m = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
			JMenuItem saveItem = new JMenuItem("Save Image");
			fileMenu.add(saveItem);
			m.add(fileMenu);
			
			saveItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) { 
					canvas.writeImage("output-" + RandomFractal.xScale + "-" +
												RandomFractal.xOff + "-" +
												RandomFractal.yOff + ".jpg");
				}
			});
			
			
			JFrame f = new JFrame("Random Fractal");
			f.setSize(RandomFractal.screenX, RandomFractal.screenY);
			
			f.setJMenuBar(m);
			f.getContentPane().add(canvas, BorderLayout.CENTER);
			f.getContentPane().add(lp, BorderLayout.SOUTH);
			f.getContentPane().add(scaleSlider, BorderLayout.WEST);
			f.getContentPane().add(xOffsetSlider, BorderLayout.NORTH);
			f.getContentPane().add(yOffsetSlider, BorderLayout.EAST);
			
			f.setVisible(true);
		}
		
		System.out.println("X = " + lastX);
		System.out.println("Y = " + lastY);
		System.out.println("Iterations = " + RandomFractal.iterations);
		
		canvas.plot(lastX, lastY, new RGB(1.0, 1.0, 1.0));
		
		long i = 0;
		boolean b = false;
		double l = 0.0;
		
		w: for (int j = 0; RandomFractal.iterations < 0 || j < RandomFractal.iterations; j++) {
			double x, y;
			
			if (args.length <= 0) {
				double xOffset = xOffsetSlider.getValue() / 1000.0 - 1.0;
				double yOffset = yOffsetSlider.getValue() / 1000.0 - 1.0;
				double scale = scaleSlider.getValue();
				
				if (lp.getValue() != l || scale != RandomFractal.xScale ||
					RandomFractal.xOff != xOffset || RandomFractal.yOff != yOffset) {
					l = lp.getValue();
					
					RandomFractal.xScale = scale;
					RandomFractal.yScale = scale;
					
					RandomFractal.xOff = xOffset;
					RandomFractal.yOff = yOffset;
					
					canvas.setXScale(RandomFractal.xScale);
					canvas.setYScale(RandomFractal.yScale);
					canvas.setXOffset(RandomFractal.xOff);
					canvas.setYOffset(RandomFractal.yOff);
					
					canvas.clear();
					lastX = 0.0;
					lastY = 0.0;
					
					i = 0;
				}
			}
			
			double d = (i / 100.0) * l;
			
			// double c[] = RandomFractal.butterfly(lastX, lastY, l, b, canvas);
			
			double c[] = RandomFractal.evaluateFuncation(lastX, lastY, canvas);
			
			lastX = c[0];
			lastY = c[1];
			
			i++;
		}
		
		System.out.println("Done");
		System.out.println("X = " + lastX);
		System.out.println("Y = " + lastY);
		
		String f = "output-" + RandomFractal.xScale + "-" +
					RandomFractal.xOff + "-" +
					RandomFractal.yOff + ".jpg";
		
		System.out.print("Writing file: ");
		canvas.writeImage(f);
		System.out.println(f);
	}
	
	public static double[] evaluateFuncation(double x, double y, ImageCanvas canvas) {
		double r = Math.random();
		
		if (Math.random() < 0.0005) System.out.println(x + " " + y);
		
		RandomFractal.xParser.addVariable(RandomFractal.xVar, x);
		RandomFractal.xParser.addVariable(RandomFractal.yVar, y);
		RandomFractal.xParser.addVariable(RandomFractal.rVar, r);
		RandomFractal.yParser.addVariable(RandomFractal.xVar, x);
		RandomFractal.yParser.addVariable(RandomFractal.yVar, y);
		RandomFractal.yParser.addVariable(RandomFractal.rVar, r);
		
		x = RandomFractal.xParser.getValue();
		y = RandomFractal.yParser.getValue();
		
		canvas.plot(x, y, RandomFractal.color);
		
		return new double[] {x, y};
	}
	
	public static double[] butterfly(double lastX, double lastY, double l, boolean b, ImageCanvas canvas) {
		double x, y;
		RGB c;
		
		// double u = Math.PI / 2.0;
		double u = 1.0;
		
		double r = Math.random();
		
		if (r < 0.5) {
			x = Math.cos(u * lastX);
			y = lastY * l;
			
			if (!b)
				c = new RGB(1.0, 1.0, 0.0);
			else
				c = new RGB(0.0, 1.0, 0.0);
			
			b = true;
		} else {
			x = lastX * l;
			y = Math.cos(u * lastY);
			
			if (b)
				c = new RGB(1.0, 0.0, 1.0);
			else
				c = new RGB(0.0, 0.0, 1.0);
			
			b = false;
		}
		
		canvas.plot(x, y, c);
		
		return new double[] {x, y};
	}
	
	public static double[] fresnelSpiral(double lastX, double lastY, double d, ImageCanvas canvas) {
		double x = lastX + Math.cos(d * d);
		double y = lastY + Math.sin(d * d);
		
		RGB c = new RGB(1.0, 0.0, 0.0);
		
//		if (Math.random() < 0.5) {
//			x = lastX + Math.cos(d * d);
//			y = lastY * 0.75;
//			
//			c = new RGB(1.0, 0.0, 0.0);
//		} else {
//			x = lastX * 0.75;
//			y = lastY + Math.sin(d * d);
//			
//			c = new RGB(0.0, 0.0, 1.0);
//		}
		
		if (canvas != null) canvas.plot(x / 1000, y / 1000, c);
		
		return new double[] {x, y};
	}
	
	public static double fresnelSin(double x) {
		int samples = 10000;
		double delta = x / samples;
		double value = 0.0;
		
		for (int i = 0; i < samples; i++) {
			value = value + delta * Math.sin((Math.PI / 2.0) * (i * i * delta * delta));
		}
		
		return value;
	}
	
	public static double fresnelCos(double x) {
		int samples = 10000;
		double delta = x / samples;
		double value = 0.0;
		
		for (int i = 0; i < samples; i++) {
			value = value + delta * Math.cos((Math.PI / 2.0) * (i * i * delta * delta));
		}
		
		return value;
	}
	
	private static void loadParsers(String xExp, String yExp) { 
		RandomFractal.xParser = new JEP();
		RandomFractal.xParser.setImplicitMul(true);
		RandomFractal.xParser.addStandardFunctions();
		RandomFractal.xParser.addStandardConstants();
		RandomFractal.xParser.addComplex();
		RandomFractal.xParser.addVariable(RandomFractal.xVar, 0);
		RandomFractal.xParser.addVariable(RandomFractal.yVar, 0);
		RandomFractal.xParser.addVariable(RandomFractal.rVar, 0);
		
		RandomFractal.yParser = new JEP();
		RandomFractal.yParser.setImplicitMul(true);
		RandomFractal.yParser.addStandardFunctions();
		RandomFractal.yParser.addStandardConstants();
		RandomFractal.yParser.addComplex();
		RandomFractal.yParser.addVariable(RandomFractal.xVar, 0);
		RandomFractal.yParser.addVariable(RandomFractal.yVar, 0);
		RandomFractal.yParser.addVariable(RandomFractal.rVar, 0);
		
		RandomFractal.xParser.parseExpression(xExp);
		
		if (RandomFractal.xParser.hasError()) {
			System.out.println("Error loading function: " + xExp);
		} else {
			System.out.println("Loaded function: " + xExp);
		}
		
		RandomFractal.yParser.parseExpression(yExp);
		if (RandomFractal.yParser.hasError()) {
			System.out.println("Error loading function: " + yExp);
		} else {
			System.out.println("Loaded function: " + yExp);
		}
	}
}
