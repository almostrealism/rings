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

package com.almostrealism.ui.panels;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JWindow;

import com.almostrealism.raytracer.Settings;

/**
 * The {@link SplashScreen} class extends {@link JWindow} and can display an
 * image while the main application loads.
 * 
 * @author Mike Murray
 */
public class SplashScreen extends JWindow {
  Image image;

	/**
	 * Constructs a new SplashScreen object to display the specified Image object.
	 */
	public SplashScreen(Image image) {
		this.image = image;
		this.setSize(720, 420);
		
		int x = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth() - this.getWidth()) / 2;
		int y = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight() - this.getHeight()) / 2;
		
		this.setLocation(x, y);
	}
	
	/**
	 * Method called to paint this SplashScreen object.
	 */
	public void paint(Graphics g) {
		g.drawString("Rings -- Version " + Settings.version, 20, 400);
		if (this.image != null) g.drawImage(this.image, 0, 0, 720, 380, this);
	}
}
