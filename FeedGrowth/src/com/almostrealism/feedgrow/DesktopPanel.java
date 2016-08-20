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
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.almostrealism.raytracer.io.WavefrontObjParser;
import com.almostrealism.replicator.Replicator;

/**
 * @author  Michael Murray
 */
public class DesktopPanel extends DesktopPanelUI {
	public DesktopPanel() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		Replicator r = new Replicator();
		
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
		
		r.addLayer("Cube", new WavefrontObjParser(Replicator.class.getResourceAsStream("/models/Cube.obj")).getMesh());
		
		btnX.addActionListener((e) -> { System.exit(0); });
		
		toolBar.add(new CanvasAction());
		toolBar.add(new ControlAction());
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
