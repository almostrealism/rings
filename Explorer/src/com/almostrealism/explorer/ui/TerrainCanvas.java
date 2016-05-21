package com.almostrealism.explorer.ui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;

import com.almostrealism.explorer.models.Terrain;
import com.almostrealism.visualize.ui.DefaultGLCanvas;

public class TerrainCanvas extends DefaultGLCanvas {
	public TerrainCanvas() {
	}
	
	public static void main(String args[]) {
		TerrainCanvas c = new TerrainCanvas();
		c.add(new Terrain(new File("Explorer/ne_110m_land/ne_110m_land.shp").toURI().toString()));
		
		JFrame frame = new JFrame("Explorer");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(c);
		
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
