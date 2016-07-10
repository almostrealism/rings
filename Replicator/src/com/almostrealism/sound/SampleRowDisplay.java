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

package com.almostrealism.sound;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.sound.util.LineUtilities;
import com.almostrealism.sound.util.SampleDisplayUtilities;

public class SampleRowDisplay extends JPanel implements Runnable, MouseListener, KeyListener {
	private Sample samples[];
	private SourceDataLine line;
	private boolean stop = true;
	
	private SampleRowColumnDisplay colDisplay;
	
	private SampleDisplayPane panels[];
	
	public SampleRowDisplay(Sample samples[], SourceDataLine line) {
		super(new GridLayout(1, samples.length));
		this.samples = samples;
		this.line = line;
		
		this.panels = new SampleDisplayPane[samples.length];
		
		for(int i = 0; i < this.panels.length; i++) {
			this.panels[i] = new SampleDisplayPane(this.samples[i], line);
			this.panels[i].setScrollingDisplay(false);
//			this.panels[i].addMouseListener(this);
			this.add(this.panels[i]);
		}
		
		this.addKeyListener(this);
	}
	
	public Dimension getPreferredSize() { return this.getMinimumSize(); }
	
	public Dimension getMinimumSize() { return new Dimension(600, 100); }
	
	public void play() { System.out.println("play"); this.playThread().start(); }
	
	public void stop() { this.stop = true; }
	
	public Thread playThread() { return new Thread(this); }
	
	public void run() {
		this.stop = false;
		
		for (int i = 0; !this.stop; i = (i + 1) % this.samples.length) {
			this.samples[i].play(line);
		}
	}
	
	public void playAndWait(int index) {
		if (this.stop == false) {
			System.out.println(this.toString() + " Sample already playing.");
			return;
		}
		
		this.stop = false;
		this.samples[index].play(line);
		this.stop = true;
	}
	
	public void setColumnDisplay(SampleRowColumnDisplay display) {
		this.colDisplay = display;
		
		for (int i = 0; i < this.panels.length; i++) {
			this.panels[i].setColumnDisplay(display);
		}
	}

	public void doSplit(Sample s) {
		if (this.colDisplay == null) {
			SampleDisplayUtilities.showSplit(s);
		} else {
			this.colDisplay.addSampleRow(s.splitLoop(8), line);
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			final Sample s = ((SampleDisplayPane)e.getSource()).getSample();
			SourceDataLine l = LineUtilities.getLine(s.getFormat());
			l.start();
			
			SampleRowDisplay rd = new SampleRowDisplay(new Sample[] { s }, l);
			
			JFrame f = new JFrame("Split");
			f.setSize(400, 250);
			f.getContentPane().add(rd);
			f.addKeyListener(rd);
			f.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_S) {
						doSplit(s);
					}
				}
			});
		} else {
			((SampleDisplayPane)e.getSource()).toggleMute();
		}
	}
	
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (this.stop)
				this.play();
			else
				this.stop();
		}
	}

	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
}
