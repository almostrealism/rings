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

package org.almostrealism.sound;

import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.SourceDataOutputLine;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class KeyBoardSampleDisplay extends JPanel implements KeyListener {
	private Sample samples[];
	private SampleDisplayPane displays[];
	private SourceDataLine lines[];
	
	public KeyBoardSampleDisplay() {
		super(new GridLayout(2, 9));
		
		this.samples = new Sample[9];
		this.displays = new SampleDisplayPane[9];
		this.lines = new SourceDataLine[9];
		
		for (int i = 1; i <= 9; i++) this.add(new JLabel(String.valueOf(i)));
		
		for (int i = 0; i < 9; i++) {
			this.displays[i] = new SampleDisplayPane(null, null);
			this.add(this.displays[i]);
		}
	}
	
	public void setSample(int index, Sample s) {
		this.samples[index - 1] = s;
		this.lines[index - 1] = ((SourceDataOutputLine) LineUtilities.getLine(s.getFormat())).getDataLine();
		this.displays[index - 1].setSample(s);
		this.displays[index - 1].setLine(this.lines[index - 1]);
	}
	
	public void playSample(int index) {
		if (this.samples[index - 1] == null) return;
		
		if (this.samples[index - 1].isStopped())
			this.samples[index - 1].playThread(this.lines[index - 1]).start();
		else
			this.samples[index - 1].restart();
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_1:
				this.playSample(1);
				break;
			case KeyEvent.VK_2:
				this.playSample(2);
				break;
			case KeyEvent.VK_3:
				this.playSample(3);
				break;
			case KeyEvent.VK_4:
				this.playSample(4);
				break;
			case KeyEvent.VK_5:
				this.playSample(5);
				break;
			case KeyEvent.VK_6:
				this.playSample(6);
				break;
			case KeyEvent.VK_7:
				this.playSample(7);
				break;
			case KeyEvent.VK_8:
				this.playSample(8);
				break;
			case KeyEvent.VK_9:
				this.playSample(9);
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
}
