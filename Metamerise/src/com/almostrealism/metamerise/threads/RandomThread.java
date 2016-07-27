package com.almostrealism.metamerise.threads;

import java.util.Iterator;
import java.util.List;

import com.almostrealism.metamerise.ui.BoundaryInputPanel;
import com.almostrealism.visual.util.ColorOutputCell;

public class RandomThread extends Thread {
	private int magnitude = 0;
	private int sleepTime = 1000;
	private int layer = 0;
	
	private BoundaryInputPanel inputPanel;
	
	public List<ColorOutputCell> cells;
	
	/**
	 * Constructs a new RandomThread which will randomly vary each color cell
	 * by up to the specified magnitude (0-255). The thread repeats this action
	 * every time it wakes from sleep, which occurs at a regular period specified
	 * by the sleep time.
	 * 
	 * @param layer  The color cell layer to operate on.
	 * @param cells  ColorOutputCells to operate on.
	 * @param magnitude  Magnitude of random color shift.
	 * @param sleepTime  Time to wait between updates.
	 */
	public RandomThread(List<ColorOutputCell> cells, int layer, int magnitude, int sleepTime) {
		this.cells = cells;
		this.magnitude = magnitude;
		this.sleepTime = sleepTime;
	}
	
	/**
	 * If set, the magnitude will increase with the vertical position of the
	 * boundary. Setting to null will disable this effect.
	 */
	public void setBoundaryInputPanel(BoundaryInputPanel s) { this.inputPanel = s; }
	
	/**
	 * Sleep / Work / Repeat
	 */
	public void run() {
		while (true) {
			Iterator<ColorOutputCell> itr = cells.iterator();
			
			try {
				Thread.sleep(this.sleepTime);
			} catch(InterruptedException ie) { }
			
			double scale = 1.0;
			
			if (this.inputPanel != null) {
				double y = inputPanel.getBoundaryY();
				scale = 1.0 - (y / inputPanel.getHeight());
			}
			
			while (itr.hasNext()) {
				ColorOutputCell c = itr.next();
				
				c.setLayerColor(this.layer,
								(int) (scale * magnitude * (Math.random() - 0.5)),
								(int) (scale * magnitude * (Math.random() - 0.5)),
								(int) (scale * magnitude * (Math.random() - 0.5)));
				c.wakeUp();
			}
		}
	}
}
