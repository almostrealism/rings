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

package com.almostrealism;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.almostrealism.swing.DragSupport;
import org.almostrealism.swing.ValueSlider;
import org.almostrealism.texture.Icons;

import com.almostrealism.feedgrow.OptimizerDesktopWidget;
import com.almostrealism.raytracer.RenderPanel;

/**
 * @author  Michael Murray
 */
public class DesktopPanel extends DesktopPanelUI {
	public static final boolean enableDraggable = false;
	
	private JFrame frame;
	
	private Replicator replicator;
	private RenderPanel raytracer;
	private ValueSlider zoomSlider;

	public DesktopPanel(JFrame parent, Replicator r) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		this.frame = parent;
		this.replicator = r;
		
		if (enableDraggable) {
			DragSupport draggable = new DragSupport(frame, this);
			addMouseListener(draggable);
			addMouseMotionListener(draggable);
		}
		
		toolBar.add(new QuitAction());
		toolBar.add(new RefreshAction());
		toolBar.add(r.getCanvasAction());
		toolBar.add(r.getLayersAction());
		toolBar.add(r.getSamplerAction());
		toolBar.add(r.getFeedbackAction());
		
		raytracer = new RenderPanel(r.getTableModel());
		renderPanel.add(raytracer, BorderLayout.CENTER);
		
		// Zoom slider
		zoomSlider = new ValueSlider(ValueSlider.VERTICAL, 0, 10, 5);
		zoomSlider.setOpaque(true);
		zoomSlider.setBackground(Color.black);
		renderPanel.add(zoomSlider, BorderLayout.EAST);
		
		// Optimizer status display
		OptimizerDesktopWidget<Long> ow = new OptimizerDesktopWidget<Long>(null);
		ow.setBackground(Color.black);
		ow.setForeground(Color.white);
		renderPanel.add(ow, BorderLayout.NORTH);
	}
	
	private static class QuitAction extends AbstractAction {
		public QuitAction() {
			super("Quit", Icons.loadImageIcon("/icons/x.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) { System.exit(0); }
	}
	
	private class RefreshAction extends AbstractAction {
		public RefreshAction() {
			super("Refresh", Icons.loadImageIcon("/icons/refresh.png"));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			replicator.getCanvas().reset();
		}
	}
}
