package com.almostrealism.explorer.ui;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;

import com.almostrealism.io.WavefrontObjParser;
import com.almostrealism.replicator.Replicator;
import com.almostrealism.visualize.ui.SurfaceCanvas;

public class TerrainCanvas extends SurfaceCanvas {
	public TerrainCanvas() {
	}
	
	public static void main(String args[]) throws IOException {
		TerrainCanvas c = new TerrainCanvas();
//		c.add(new Terrain(new File("Explorer/ne_110m_land/ne_110m_land.shp").toURI().toString()));
		c.addSurface(WavefrontObjParser.parse(Replicator.class.getResourceAsStream("/models/Cube.obj")));
//		c.add(new Gear(1.3f, 2.0f, 0.5f, 10, 0.7f));
		
		c.start();
		
		JFrame frame = new JFrame("Explorer");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(c);
		
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
