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

import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.almostrealism.NetworkClient;

/**
 * @author  Michael Murray
 */
public class Desktop extends JFrame {
	public static final boolean enableTranslucency = false;
	
	public Desktop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		super("Rings");
		setLayout(new BorderLayout());
		
		setUndecorated(true);
		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		add(new DesktopPanel(this, new Replicator()), BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		final boolean isTranslucencySupported = gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
		
		if (!isTranslucencySupported) {
			System.out.println("Translucency is not supported");
		}
		
		SwingUtilities.invokeLater(() -> {
			Desktop d = null;
			
			try {
				d = new Desktop();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			if (enableTranslucency && isTranslucencySupported) d.setOpacity(0.8f);
			
			// Display the window.
			d.setVisible(true);
		});
		
		NetworkClient.main(new String[0]);
	}
}