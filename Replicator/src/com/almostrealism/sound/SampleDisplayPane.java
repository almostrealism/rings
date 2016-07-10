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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;

public class SampleDisplayPane extends JPanel implements MouseListener, KeyListener {
	private Sample sample;
	private SourceDataLine line;
	
	private KeyBoardSampleDisplay keyboardSamples;
	private SampleRowColumnDisplay columnDisplay;
	
	private int dsr = 500;
	private boolean scroll = true;
	
	public SampleDisplayPane(Sample s, SourceDataLine line) {
		this.sample = s;
		this.line = line;
		
		this.addMouseListener(this);
	}
	
	public void setSample(Sample s) { this.sample = s; this.repaint(); }
	public Sample getSample() { return this.sample; }
	
	public void setLine(SourceDataLine line) { this.line = line; }
	
	public void setDownsampleRate(int dsr) { this.dsr = dsr; }
	public double getDownsampleRate() { return this.dsr; }
	
	public void setScrollingDisplay(boolean scroll) { this.scroll = scroll; }
	
	public void setColumnDisplay(SampleRowColumnDisplay rcd) {
		this.columnDisplay = rcd;
		this.setKeyBoardSampleDisplay(rcd.getKeyBoardSampleDisplay());
	}
	
	public void setKeyBoardSampleDisplay(KeyBoardSampleDisplay kd) {
		this.keyboardSamples = kd;
	}
	
	public KeyBoardSampleDisplay getKeyBoardSampleDisplay() {
		return this.keyboardSamples;
	}
	
	public void paint(Graphics g) {
		int w = getWidth();
		
		g.setColor(Color.black);
		g.fillRect(0, 0, w, getHeight());
		
		if (this.sample == null) return;
		
		double r = getHeight() / 480.0;
		
		int pos = sample.pos;
		int index = -1;
		w = w / 2;
		
		Color scolor = Color.blue;
		if (sample.isMuted()) scolor = Color.white;
		
		g.setColor(scolor);
		i: for (int i = 0; i < (w * 2); i++) {
			if (scroll)
				index = pos + (i - w) * dsr;
			else
				index = i * dsr;
			
			if (index < 0) continue i;
			if (index >= sample.data.length) continue i;
			
			if (i == w && scroll) {
				g.setColor(Color.red);
				g.drawLine(i, 0, i, getHeight());
				g.setColor(scolor);
			} else if (index >= sample.loopStart && index <= sample.loopEnd) {
				g.setColor(Color.green);
				g.drawLine(i, 0, i, getHeight());
				g.setColor(scolor);
			}
			
			g.drawLine(i, (int) (140 * r), i, (int) ((140 + sample.data[index][0]) * r));
			
			if (sample.data[index].length > 1)
				g.drawLine(i, (int) (360 * r), i, (int) ((360 + sample.data[index][1]) * r));
		}
		
//		g.setColor(Color.green);
//		for (int i = 0; i < sample.realFFT.length; i++)
//			g.drawLine(i, 460, i, 460 + (int) (sample.realFFT[i] * 100));
	}
	
	public int getPos(int x) {
		if (this.scroll)
			return sample.pos + dsr * (x - getWidth() / 2);
		else
			return x * dsr;
	}
	
	public void toggleMute() {
		this.sample.toggleMute();
		this.repaint();
	}
	
	public void removeMouseListener() { this.removeMouseListener(this); }
	
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	
	public void mousePressed(MouseEvent e) {
		System.out.println(this.columnDisplay + " " + this.keyboardSamples + " " + this.sample);
		
		if (this.sample == null) return;
		
		if (this.columnDisplay != null && this.keyboardSamples != null) {
			int key = this.columnDisplay.getKeyBoardSampleIndex();
			
			if (key > 0) {
				this.keyboardSamples.setSample(key, new Sample(this.sample.data, this.sample.getFormat()));
				return;
			}
		}
		
		if ((e.getModifiers() & MouseEvent.ALT_MASK) != 0) {
			this.toggleMute();
		} else if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0) {
			this.sample.loopEnd = Math.min(this.getPos(e.getX()), sample.data.length);
			System.out.println("loopEnd = " + this.sample.loopEnd);
		} else {
			this.sample.loopStart = Math.max(this.getPos(e.getX()), 0);
			System.out.println("loopStart = " + this.sample.loopStart);
		}
		
		this.repaint();
	}

	public void mouseReleased(MouseEvent e) { }

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (this.sample.isStopped())
				this.sample.playThread(this.line).start();
			else
				this.sample.stop();
		}
	}

	public void keyReleased(KeyEvent e) { }
	public void keyTyped(KeyEvent e) { }
}
