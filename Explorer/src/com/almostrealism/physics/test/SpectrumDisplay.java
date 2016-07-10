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

package com.almostrealism.physics.test;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;

public class SpectrumDisplay extends JPanel {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpectrumDisplay d = new SpectrumDisplay();
		JFrame frame = new JFrame("Spectrum");
		frame.getContentPane().add(d);
		frame.setSize(400, 60);
		frame.setVisible(true);
	}
	
	public void paint(Graphics g) {
		int off = 380;
		
		for (int i = 0; i < (780 - 380); i++) {
			g.setColor(GraphicsConverter.convertToAWTColor(new RGB((double)(off + i))));
			g.drawLine(i * 2, 0, i * 2, 40);
			g.drawLine(i * 2 + 1, 0, i * 2 + 1, 40);
		}
	}
}
