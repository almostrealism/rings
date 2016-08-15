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
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author  Michael Murray
 */
public class Desktop extends JFrame {
	public Desktop() {
		super("Rings");
		setLayout(new GridBagLayout());
		
		setUndecorated(true);
		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(new JButton("I am a Button"));
	}

	public static void main(String[] args) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		final boolean isTranslucencySupported = gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
		
		if (!gd.isWindowTranslucencySupported(WindowTranslucency.PERPIXEL_TRANSPARENT)) {
			// Rings Desktop requires shaped windows
			System.err.println("Shaped windows are not supported");
			System.exit(0);
		}
		
		if (!isTranslucencySupported) {
			System.out.println("Translucency is not supported");
		}
		
		SwingUtilities.invokeLater(() -> {
			Desktop d = new Desktop();
			
			if (isTranslucencySupported) d.setOpacity(0.7f);
			
			// Display the window.
			d.setVisible(true);
		});
	}
}
