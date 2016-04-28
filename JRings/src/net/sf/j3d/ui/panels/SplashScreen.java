/*
* Copyright (C) 2004-05  Mike Murray
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

package net.sf.j3d.ui.panels;


import java.awt.*;
import javax.swing.*;

import net.sf.j3d.run.Settings;

/**
 * The SplashScreen class extends JWindow and can display an image while the main application loads.
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
