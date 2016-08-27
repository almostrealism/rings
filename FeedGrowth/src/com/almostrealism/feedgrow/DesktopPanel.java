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

package com.almostrealism.feedgrow;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.almostrealism.raytracer.io.WavefrontObjParser;
import com.almostrealism.receptor.ReceptorRenderPanel;

/**
 * @author  Michael Murray
 */
public class DesktopPanel extends DesktopPanelUI {
    private Point initialClick;
    private JFrame frame;
	
	public DesktopPanel(JFrame parent, Replicator r) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
	    this.frame = parent;

	    addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            initialClick = e.getPoint();
	            getComponentAt(initialClick);
	        }
	    });
	    
	    addMouseMotionListener(new MouseMotionAdapter() {
	        @Override
	        public void mouseDragged(MouseEvent e) {

	            // get location of Window
	            int thisX = frame.getLocation().x;
	            int thisY = frame.getLocation().y;

	            // Determine how much the mouse moved since the initial click
	            int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
	            int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

	            // Move window to this position
	            int X = thisX + xMoved;
	            int Y = thisY + yMoved;
	            frame.setLocation(X, Y);
	        }
	    });
		
		CanvasAction.canvasFrame = new JFrame("Replicant");
		CanvasAction.canvasFrame.setLayout(new BorderLayout());
		CanvasAction.canvasFrame.getContentPane().add(r.getCanvas());
		CanvasAction.canvasFrame.setSize(400, 400);
		CanvasAction.canvasFrame.setLocationRelativeTo(null);
		CanvasAction.canvasFrame.setVisible(false);
		
		ControlAction.controlFrame = new JFrame("");
		ControlAction.controlFrame.setLayout(new BorderLayout());
		ControlAction.controlFrame.getContentPane().add(r.getControlPanel());
		ControlAction.controlFrame.setLocation(CanvasAction.canvasFrame.getLocation().x +
								CanvasAction.canvasFrame.getWidth(),
								CanvasAction.canvasFrame.getLocation().y);
		ControlAction.controlFrame.setSize(90, 140);
		ControlAction.controlFrame.setVisible(false);
		
		r.addLayer("Cube", new WavefrontObjParser(Replicator.class.getClassLoader().getResourceAsStream("models/Cube.obj")).getMesh());
		
		toolBar.add(new QuitAction());
		toolBar.add(new CanvasAction());
		toolBar.add(new ControlAction());
		
		ReceptorRenderPanel raytracer = new ReceptorRenderPanel(r.getScene());
		
		add(raytracer, BorderLayout.CENTER);
	}
	
	private static class QuitAction extends AbstractAction {
		public QuitAction() {
			super("Quit", new ImageIcon(CanvasAction.class.getResource("/icons/x.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { System.exit(0); }
	}
	
	private static class CanvasAction extends AbstractAction {
		static JFrame canvasFrame;
		
		public CanvasAction() {
			super("Viewer", new ImageIcon(CanvasAction.class.getResource("/icons/v.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { canvasFrame.setVisible(true); }
	}
	
	private static class ControlAction extends AbstractAction {
		static JFrame controlFrame;
		
		public ControlAction() {
			super("Control", new ImageIcon(CanvasAction.class.getResource("/icons/e.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { controlFrame.setVisible(true); }
	}
}
