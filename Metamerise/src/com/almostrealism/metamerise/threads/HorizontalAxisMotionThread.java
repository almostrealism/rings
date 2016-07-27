package com.almostrealism.metamerise.threads;

import java.util.Iterator;
import java.util.List;

import com.almostrealism.metamerise.ui.BoundaryInputPanel;
import com.almostrealism.visual.util.ColorOutputCell;

public class HorizontalAxisMotionThread extends Thread {
	public List<ColorOutputCell> cells;
	private BoundaryInputPanel inputPanel;
	
	public HorizontalAxisMotionThread(BoundaryInputPanel p, List<ColorOutputCell> cells) {
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
			
			double w = inputPanel.getWidth();
			double x = inputPanel.getBoundaryX();
			
			double alpha;
			
			if (x < w / 2) {
				alpha = 2 * (x / w);
			} else {
				x = x - w / 2;
				alpha = 1.0 - 2 * (x / w);
			}
			
			while (itr.hasNext()) {
				ColorOutputCell c = itr.next();
				c.setAlpha(alpha);
				c.wakeUp();
			}
		}
	}
}
