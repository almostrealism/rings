package com.almostrealism.metamerise.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.metamerise.threads.DepthAxisMotionThread;
import com.almostrealism.metamerise.threads.HorizontalAxisMotionThread;
import com.almostrealism.metamerise.threads.RandomThread;
import com.almostrealism.visual.util.ColorOutputCell;

/**
 * The grid display provides a frame with a regular arrangement of
 * color output cells.
 */
public class GridDisplay extends JPanel {
	private ColorOutputCell cells[][];
	
	/**
	 * Construct the display.
	 * 
	 * @param w  Width (color output cells)
	 * @param h  Height (color output cells)
	 */
	public GridDisplay(int w, int h, int layers) {
		this.cells = new ColorOutputCell[w][h];
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				this.cells[i][j] = new ColorOutputCell(this, layers);
			}
		}
	}
	
	public void paint(Graphics g) {
		int w = this.getWidth() / cells.length;
		int h = this.getHeight() / cells[0].length;
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				int rgb[] = cells[i][j].getFlattenedColor();
				Color c = new Color(rgb[0], rgb[1], rgb[2]);
				g.setColor(c);
				g.fillRect(i * w, j * h, w, h);
			}
		}
	}
	
	public List<ColorOutputCell> getCells() {
		ArrayList<ColorOutputCell> l = new ArrayList<ColorOutputCell>();
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[0].length; j++) {
				l.add(this.cells[i][j]);
			}
		}
		
		return l;
	}
	
	/**
	 * Start the display.
	 */
	public static void main(String args[]) {
		GridDisplay gd = new GridDisplay(1, 8, 2);
		
		gd.getCells().get(0).setRedChannel(2);
		gd.getCells().get(0).setGreenChannel(3);
		gd.getCells().get(0).setBlueChannel(4);
		
		gd.getCells().get(1).setRedChannel(6);
		gd.getCells().get(1).setGreenChannel(7);
		gd.getCells().get(1).setBlueChannel(8);
		
		gd.getCells().get(2).setRedChannel(10);
		gd.getCells().get(2).setGreenChannel(11);
		gd.getCells().get(2).setBlueChannel(12);
		
		gd.getCells().get(3).setRedChannel(14);
		gd.getCells().get(3).setGreenChannel(15);
		gd.getCells().get(3).setBlueChannel(16);
		
		gd.getCells().get(4).setRedChannel(18);
		gd.getCells().get(4).setGreenChannel(19);
		gd.getCells().get(4).setBlueChannel(20);
		
		gd.getCells().get(5).setRedChannel(22);
		gd.getCells().get(5).setGreenChannel(23);
		gd.getCells().get(5).setBlueChannel(24);
		
		gd.getCells().get(6).setRedChannel(26);
		gd.getCells().get(6).setGreenChannel(27);
		gd.getCells().get(6).setBlueChannel(28);
		
		gd.getCells().get(7).setRedChannel(30);
		gd.getCells().get(7).setGreenChannel(31);
		gd.getCells().get(7).setBlueChannel(32);
		
//		Thread t = new RandomThread(gd.getCells(), 1, 70, 50);
//		t.start();
		
		JFrame f = new JFrame("Truss View [AR MEDIA]");
		f.add(gd);
		f.setSize(100, 800);
		f.setResizable(false);
		f.setVisible(true);
		
		
		BoundaryInputPanel p = new BoundaryInputPanel();
		BoundaryControlPanel c = new BoundaryControlPanel(p, 500, 500);
		
		Thread dAxisThread = new DepthAxisMotionThread(p, gd.getCells());
		dAxisThread.start();
		
		Thread hAxisThread = new HorizontalAxisMotionThread(p, gd.getCells());
		hAxisThread.start();
		
		RandomThread random = new RandomThread(gd.getCells(), 1, 140, 50);
		random.setBoundaryInputPanel(p);
		random.start();
		
		JFrame pPanel = new JFrame("Boundary Input [AR MEDIA]");
		pPanel.add(p);
		pPanel.setSize(500, 500);
		pPanel.setLocation(140, 0);
		pPanel.setResizable(false);
		pPanel.setVisible(true);
		
		JFrame cPanel = new JFrame("Boundary Controls [AR MEDIA]");
		cPanel.add(c);
		cPanel.setSize(500, 200);
		cPanel.setLocation(140, 540);
		cPanel.setVisible(true);
	}
}
