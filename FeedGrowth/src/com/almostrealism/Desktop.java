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

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import org.almostrealism.color.RGB;

import com.almostrealism.raytracer.primitives.Sphere;

/**
 * @author  Michael Murray
 */
public class Desktop extends JFrame {
	public Desktop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		super("Rings");
		
		setUndecorated(true);
		setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Replicator r = new Replicator();
		
		Sphere s = new Sphere(2.0);
		s.setColor(RGB.gray(0.8));
		r.addLayer("Sphere", s);
		
		r.start();
		
		getContentPane().add(new DesktopPanel(this, r));
	}
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		final Desktop d = new Desktop();
		d.setVisible(true);
		NetworkClient.main(new String[0]);
	}
}
