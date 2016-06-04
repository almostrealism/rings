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

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;
import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.ui.displays.ProgressDisplay;
import net.sf.j3d.ui.event.Event;
import net.sf.j3d.ui.event.EventGenerator;
import net.sf.j3d.ui.event.EventHandler;
import net.sf.j3d.ui.event.EventListener;
import net.sf.j3d.ui.event.SceneCloseEvent;
import net.sf.j3d.ui.event.SceneOpenEvent;
import net.sf.j3d.ui.event.SurfaceEditEvent;


/**
 * A RenderPanel object allows display of scene previews and rendered images of the Scene object it uses.
 * 
 * @author Mike Murray
 */
public class RenderPanel extends JPanel implements EventListener, EventGenerator {
  private Scene scene;
  private EventHandler handler;
  
  private boolean showProgressWindow;
  
  private int width, height, ssWidth, ssHeight;
  
  private RGB renderedImageData[][];
  private java.awt.Image renderedImage;

	/**
	 * Constructs a new RenderPanel object that can be used to render the specified Scene object.
	 */
	public RenderPanel(Scene scene) {
		super(new java.awt.FlowLayout());
		
		this.scene = scene;
		
		double ph = this.getProjectionHeight();
		double pw = this.getProjectionWidth();
		int w = 100;
		
		this.setImageWidth(w);
		this.setImageHeight((int)(ph * (w / pw)));
		this.setSupersampleWidth(1);
		this.setSupersampleHeight(1);
		
		this.setShowProgressWindow(false);
	}
	
	/**
	 * Renders the Scene object stored by this RenderPanel object and draws the output image on the panel.
	 */
	public void render() {
		int totalPixels = this.getImageWidth() * this.getImageHeight();
		
		final ProgressDisplay display = new ProgressDisplay(totalPixels / 100, totalPixels);
		final JButton cancelButton = new JButton("Cancel");
		
		JFrame frame = null;
		
		if (this.showProgressWindow == true) {
			frame = new JFrame("Rendering...");
			frame.setSize(300, 80);
			
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(display);
			frame.getContentPane().add(cancelButton);
		} else {
			this.clearRenderedImage();
			
			display.setRemoveOnCompletion(true);
			this.add(display);
			this.add(cancelButton);
		}
		
		final Thread renderThread = new Thread(new Runnable() {
			public void run() {
				renderedImageData = RayTracingEngine.render(scene, getImageWidth(), getImageHeight(), getSupersampleWidth(), getSupersampleHeight(), display);
				renderedImage = GraphicsConverter.convertToAWTImage(renderedImageData);
				
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							RenderPanel.this.removeAll();
							revalidate();
							repaint();
						}
					});
				} catch(InterruptedException ie) {
					System.out.println("Swing Utilities Interruption: " + ie.toString());
				} catch(java.lang.reflect.InvocationTargetException ite) {
					System.out.println("Swing Utilities Invocation Target Error: " + ite.toString());
				}
			}
		});
		
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				renderThread.stop();
				System.gc();
				
				if (renderThread.isInterrupted()) {
					RenderPanel.this.removeAll();
					RenderPanel.this.revalidate();
					RenderPanel.this.repaint();
				}
			}
		});
		
		if (this.showProgressWindow == true) {
			frame.setVisible(true);
		}
		
		renderThread.start();
		
		this.revalidate();
		this.repaint();
	}
	
	/**
	 * Removes any stored image data from this RenderPanel object and repaints the panel.
	 */
	public void clearRenderedImage() {
		this.renderedImageData = null;
		this.renderedImage = null;
		this.repaint();
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
			this.clearRenderedImage();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
			this.clearRenderedImage();
		}
		
		if (event instanceof SurfaceEditEvent) {
			this.repaint();
		}
	}
	
	/**
	 * Sets the width, in pixels, of the image that will be rendered by this RenderPanel object.
	 */
	public void setImageWidth(int width) { this.width = width; }
	
	/**
	 * Sets the height, in pixels, of the image that will be rendered by this RenderPanel object.
	 */
	public void setImageHeight(int height) { this.height = height; }
	
	/**
	 * Sets the supersampling width of the image that will be rendered by this RenderPanel object.
	 */
	public void setSupersampleWidth(int width) { this.ssWidth = width; }
	
	/**
	 * Sets the supersampling height of the image that will be rendered by this RenderPanel object.
	 */
	public void setSupersampleHeight(int height) { this.ssHeight = height; }
	
	/**
	 * @return  The projection width of the Camera object stored by this RenderPanel object.
	 */
	public double getProjectionWidth() {
		if (this.scene.getCamera() instanceof OrthographicCamera)
			return ((OrthographicCamera)this.scene.getCamera()).getProjectionWidth();
		else
			return 0.0;
	}
	
	/**
	 * @return  The projection height of the Camera object stored by this RenderPanel object.
	 */
	public double getProjectionHeight() {
		if (this.scene.getCamera() instanceof OrthographicCamera)
			return ((OrthographicCamera)this.scene.getCamera()).getProjectionHeight();
		else
			return 0.0;
	}
	
	/**
	 * When set to true, rendering progress will be displayed in a new window.
	 * Otherwise, the progress will be displayed within this RenderPanel's window.
	 */
	public void setShowProgressWindow(boolean show) { this.showProgressWindow = show; }
		
	/**
	 * Returns the width, in pixels, of the image that will be rendered by this RenderPanel object.
	 */
	public int getImageWidth() { return this.width; }
	
	/**
	 * Returns the height, in pixels, of the image that will be rendered by this RenderPanel object.
	 */
	public int getImageHeight() { return this.height; }
	
	/**
	 * Returns the supersampling width of the image that will be rendered by this RenderPanel object.
	 */
	public int getSupersampleWidth() { return this.ssWidth; }
	
	/**
	 * Returns the supersampling height of the image that will be rendered by this RenderPanel object.
	 */
	public int getSupersampleHeight() { return this.ssHeight; }
	
	/**
	 * Returns true if rendering progress will be displayed in a new window.
	 * Otherwise, false is returned and the progress will be displayed within this RenderPanel's window.
	 */
	public boolean getShowProgressWindow() { return this.showProgressWindow; }
	
	/**
	 * Return the image rendered by this RenderPanel as an array of RGB objects.
	 */
	public RGB[][] getRenderedImageData() { return this.renderedImageData; }
	
	/**
	 * Sets the EventHandler object used by this RenderPanel object. Setting this to null will deactivae event reporting.
	 */
	public void setEventHandler(EventHandler handler) { this.handler = handler; }
	
	/**
	 * Returns the EventHandler object used by this RenderPanel object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
	
	public double calculateAverageBrightness() {
		return RayTracingEngine.calculateAverageBrightness(this.scene, this.width, this.height, 3);
	}
	
	/**
	 * Method called when painting this RenderPanel.
	 */
	public void paint(Graphics g) {
		if (this.renderedImage != null) {
			g.setColor(Color.black);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.drawImage(this.renderedImage, 0, 0, this);
		} else {
			g.setColor(Color.black);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			if (this.scene != null) {
				for(int i = 0; i < this.scene.getSurfaces().length; i++) {
					Surface surface = this.scene.getSurface(i);
					
					if (surface instanceof SurfaceUI) {
						((SurfaceUI)surface).draw(g, this.scene.getCamera());
					}
				}
			}
			
			super.paint(g);
		}
	}
}
