package com.almostrealism.visual.util;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import com.almostrealism.metamerise.dmx.DMXClient;

/**
 * The color output cell is the main device used by Metamerise.
 * It can be routed to DMX, or other 3 channel RGB output.
 */
public class ColorOutputCell {
	private Container panel;
	private int r, g, b;
	private double alpha = 1.0;
	
	private int layers[][];
	private int dmxChannels[] = {-1, -1, -1, -1};
	
	/**
	 * Constructs a new ColorOutputCell for display in the specified
	 * container and with the specified number of layers.
	 */
	public ColorOutputCell(Container panel, int layers) {
		this.panel = panel;
		this.layers = new int[layers][3];
	}
	
	/**
	 * Set the alpha parameter, which determines the overall
	 * brightness of the cell. Should be between zero and one,
	 * but this is not enforced.
	 */
	public void setAlpha(double a) { this.alpha = a; }
	
	/**
	 * Returns the alpha parameter, which determines the overall
	 * brightness of the cell. Should be between zero and one,
	 * but this is not enforced.
	 */
	public double getAlpha() { return this.alpha; }
	
	/**
	 * Returns the base color of the cell.
	 */
	public int[] getColor() { return new int[] {r, g, b}; }
	
	/**
	 * Returns the current displayed color of the cell.
	 */
	public int[] getFlattenedColor() {
		int r = this.r;
		int g = this.g;
		int b = this.b;
		
		for (int i = 0; i < this.layers.length; i++) {
			r = r + this.layers[i][0];
			g = g + this.layers[i][1];
			b = b + this.layers[i][2];
		}
		
		r = (int) (r * alpha);
		g = (int) (g * alpha);
		b = (int) (b * alpha);
		
		r = Math.max(0, r);
		r = Math.min(255, r);
		g = Math.max(0, g);
		g = Math.min(255, g);
		b = Math.max(0, b);
		b  = Math.min(255, b);
		
		return new int[] {r, g, b};
	}
	
	/**
	 * Sets the color of the cell.
	 */
	public void setColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		
		this.r = Math.max(0, r);
		this.r = Math.min(255, r);
		this.g = Math.max(0, g);
		this.g = Math.min(255, g);
		this.b = Math.max(0, b);
		this.b  = Math.min(255, b);
	}
	
	/**
	 * Sets the color of the specified layer. All layers
	 * are flattened by RGB addition when the color is
	 * used for display.
	 * 
	 * @see  #getFlattenedColor()
	 */
	public void setLayerColor(int layer, int r, int g, int b) {
		this.layers[layer][0] = r;
		this.layers[layer][1] = g;
		this.layers[layer][2] = b;
	}
	
	/**
	 * Tells the container to repaint and notifies the DMX
	 * client to update the color of the cell.
	 * 
	 * @see  Container#repaint()
	 */
	public void wakeUp() {
		this.panel.repaint();
		
		List<String[]> channels = new ArrayList<String[]>();
		
		if (this.dmxChannels[1] > 0) {
			channels.add(new String[] {String.valueOf(dmxChannels[1]), String.valueOf((int) (100 * r / 255.0))});
		}
		
		if (this.dmxChannels[2] > 0) {
			channels.add(new String[] {String.valueOf(dmxChannels[2]), String.valueOf((int) (100 * g / 255.0))});
		}
		
		if (this.dmxChannels[3] > 0) {
			channels.add(new String[] {String.valueOf(dmxChannels[3]), String.valueOf((int) (100 * b / 255.0)) });
		}
		
		DMXClient.getDefaultDMXClient().setChannels((String[][]) channels.toArray(new String[0][0]));
	}
	
	/**
	 * Set the DMX channel which controls the brightness of this cell.
	 */
	public void setBrightnessChannel(int channel) { this.dmxChannels[0] = channel; }
	
	
	/**
	 * Set the DMX channel which controls the red level of this cell.
	 */
	public void setRedChannel(int channel) { this.dmxChannels[1] = channel; }
	
	
	/**
	 * Set the DMX channel which controls the green level of this cell.
	 */
	public void setGreenChannel(int channel) { this.dmxChannels[2] = channel; }
	
	
	/**
	 * Set the DMX channel which controls the blue level of this cell.
	 */
	public void setBlueChannel(int channel) { this.dmxChannels[3] = channel; }
}