/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.sound.test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.SourceDataOutputLine;
import org.almostrealism.sound.KeyBoardSampleDisplay;
import org.almostrealism.sound.Sample;
import org.almostrealism.sound.SampleDisplayPane;
import org.almostrealism.sound.util.SampleDisplayUtilities;

public class SoundExperiment {
	public static void main(String args[]) throws UnsupportedAudioFileException, IOException {
		JFileChooser f = new JFileChooser();
		f.showOpenDialog(null);
		
		if (f.getSelectedFile() == null) return;
		
		AudioInputStream in = AudioSystem.getAudioInputStream(f.getSelectedFile());
		AudioFormat format = in.getFormat();
		
		int frameSize = format.getFrameSize();
		double frameRate = format.getFrameRate();
		
		System.out.println("Frame size = " + frameSize);
		System.out.println("Frame rate = " + frameRate);
		System.out.println(in.getFrameLength() + " frames");
		
		System.out.print("Opening output lines: ");
		
		SourceDataLine line = ((SourceDataOutputLine) LineUtilities.getLine(format)).getDataLine();
		
		System.out.println("Open");
		
		byte data[][] = new byte[(int) in.getFrameLength()][frameSize];
		
		for (int l = 0; l < in.getFrameLength(); l++) {
			in.read(data[l]);
		}
		
		final Sample s = new Sample(data, format);
		
		final SampleDisplayPane ds = new SampleDisplayPane(s, line);
		
		JFrame frame = new JFrame("JavaAudioSample");
		frame.getContentPane().add(ds);
		frame.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_S) {
					SampleDisplayUtilities.showColumnDisplay(s);
				} else {
					ds.keyPressed(e);
				}
			}

			public void keyReleased(KeyEvent e) {
				ds.keyReleased(e);
			}

			public void keyTyped(KeyEvent e) {
				ds.keyTyped(e);
			}
			
		});
		frame.setSize(500, 200);
		frame.setVisible(true);
		
		KeyBoardSampleDisplay kd = new KeyBoardSampleDisplay();
		ds.setKeyBoardSampleDisplay(kd);
		
		JFrame keyboardSamplesFrame = new JFrame("Key Board Samples");
		keyboardSamplesFrame.getContentPane().add(kd);
		keyboardSamplesFrame.addKeyListener(kd);
		keyboardSamplesFrame.setSize(500, 200);
		keyboardSamplesFrame.setVisible(true);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					ds.repaint();
				}
			}
		});
		t.start();
		
		line.start();
	}
}
