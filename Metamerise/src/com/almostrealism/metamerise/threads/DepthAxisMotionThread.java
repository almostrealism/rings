package com.almostrealism.metamerise.threads;

import java.util.Iterator;
import java.util.List;

import com.almostrealism.metamerise.ui.BoundaryInputPanel;
import com.almostrealism.visual.util.ColorOutputCell;

public class DepthAxisMotionThread extends Thread {
	public List<ColorOutputCell> cells;
	private BoundaryInputPanel inputPanel;
	
	public DepthAxisMotionThread(BoundaryInputPanel p, List<ColorOutputCell> cells) {
		this.inputPanel = p;
		this.cells = cells;
	}
	
	/**
	 * Sleep / Work / Repeat
	 */
	public void run() {
		while (true) {
			Iterator<ColorOutputCell> itr = cells.iterator();
			
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) { }
			
			int i = 1;
			
			while (itr.hasNext()) {
				ColorOutputCell c = itr.next();
				double scale = inputPanel.getBoundaryScale();
				
				scale = (scale * 2.0) - ((i - 4) * 1/8.0);
				
				scale = Math.abs(scale - 0.5);
				
				int r, g, b;
				
				if (scale < 1.0/6.0) {
					r = 255;
					g = (int) (255 / (255 * scale * 5.0));
					b = 0;
				} else if (scale < 2.0/6.0) {
					scale = scale - 1.0/6.0;
					
					r = 255 - ((int) (255 * scale * 5.0));
					g = 255;
					b = 0;
				} else if (scale < 3.0/6.0) {
					scale = scale - 2.0/6.0;
					
					r = 0;
					g = 255;
					b = (int) (255 / (255 * scale * 5.0));
				} else if (scale < 4.0/6.0) {
					scale = scale - 3.0/6.0;
					
					r = 0;
					g = 255 - ((int) (255 * scale * 5.0));
					b = 255;
				} else if (scale < 5.0/6.0) {
					scale = scale - 4.0/6.0;
					
					r = (int) (255 * scale * 5.0);
					g = 0;
					b = 255;
				} else if (scale < 1.0) {
					scale = scale - 5.0/6.0;
					
					r = 255;
					b = 255 - ((int) (255 * scale * 5.0));
					g = 0;
				} else {
					r = 0;
					g = 0;
					b = 0;
				}
				
				r = Math.max(0, r);
				r = Math.min(255, r);
				g = Math.max(0, g);
				g = Math.min(255, g);
				b = Math.max(0, b);
				b = Math.min(255, b);
				
				c.setColor(r, g, b);
				c.wakeUp();
				
				i++;
			}
		}
	}
}
