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

import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author  Michael Murray
 */
public class Desktop extends JFrame {
	public Desktop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		super("Rings");
		setLayout(new GridBagLayout());
		
		setUndecorated(true);
		setSize(200, 100);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		add(new DesktopPanel());
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
			
			if (isTranslucencySupported) d.setOpacity(0.7f);
			
			// Display the window.
			d.setVisible(true);
		});
	}
}
