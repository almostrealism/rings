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

package com.almostrealism.photon.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.almostrealism.ui.displays.ImageCanvas;
import org.almostrealism.util.graphics.RGB;

import com.almostrealism.photon.texture.CosineIntensityMap;
import com.almostrealism.photon.texture.IntensityMap;
import com.almostrealism.photon.texture.Turbulence;

public class IntensityMapPanel extends JPanel {
	private IntensityMap map;
	private ImageCanvas display;
	
	public static void main(String args[]) throws InstantiationException,
											IllegalAccessException,
											ClassNotFoundException {
		IntensityMap map;
		
		if (args.length > 0) {
			map = (IntensityMap) Class.forName(args[0]).newInstance();
		} else {
			map = new CosineIntensityMap(new Turbulence());
		}
		
		ImageCanvas canvas = new ImageCanvas(600, 600);
		IntensityMapPanel panel = new IntensityMapPanel(map, canvas);
		
		JFrame display = new JFrame("Intensity");
		JFrame frame = new JFrame("Map");
		
		display.getContentPane().add(canvas);
		frame.getContentPane().add(panel);
		
		display.setSize(250, 600);
		frame.setSize(100, 70);
		frame.setLocation(300, 30);
		
		display.setVisible(true);
		frame.setVisible(true);
	}
	
	public IntensityMapPanel(IntensityMap map, ImageCanvas display) {
		this.map = map;
		this.display = display;
		
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				IntensityMapPanel.this.updateDisplay();
			}
		});
		
		super.add(updateButton);
	}
	
	public void updateDisplay() {
		int w = this.display.getWidth();
		int h = this.display.getHeight();
		RGB image[][] = new RGB[w][h];
		
		for (int i = 0; i < w; i++) {
			double u = ((double)i)/((double)w);
			
			for (int j = 0; j < h; j++) {
				double v = ((double)j)/((double)h);
				
				if (v == 0.0)
					v = 0.9999999999;
				else
					v = 1.0 - v;
				
				double in = this.map.getIntensity(u, v, 0.0);
				image[i][j] = new RGB(in, in, in);
			}
		}
		
		this.display.setImageData(image);
	}
}
