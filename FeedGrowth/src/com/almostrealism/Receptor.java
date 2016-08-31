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
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import org.almostrealism.cells.CellAdjustment;
import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.DoubleScaleFactor;
import org.almostrealism.organs.AdjustmentLayerOrganSystem;
import org.almostrealism.organs.CellAdjustmentFactory;
import org.almostrealism.protein.FloatingPointProteinCache;

import com.almostrealism.audio.AudioProteinCache;
import com.almostrealism.audio.Mixer;
import com.almostrealism.audio.filter.Envelope;
import com.almostrealism.audio.filter.PeriodicCellAdjustment;
import com.almostrealism.feedgrow.ReceptorPlayer;
import com.almostrealism.feedgrow.test.BasicDyadicCellularSystem;
import com.almostrealism.feedgrow.test.BasicDyadicChromosome;
import com.almostrealism.receptor.ReceptorPlayerPanel;
import com.almostrealism.synth.SineWaveCell;

public class Receptor {
	public static Mixer globalMixer;
	
	private JFrame receptorFrame, feedbackFrame;
	
	private ReceptorPlayerPanel panel;
	
	public Receptor() throws UnsupportedAudioFileException, IOException {
		panel = new ReceptorPlayerPanel();
		
		feedbackFrame = new JFrame("Feedback");
		feedbackFrame.getContentPane().setLayout(new BorderLayout());
		feedbackFrame.getContentPane().add(panel, BorderLayout.CENTER);
		feedbackFrame.setSize(400, 250);
	}
	
	public void setPlayer(ReceptorPlayer p) { panel.setReceptorPlayer(p); }
	
	public ReceptorPlayerPanel getPlayerPanel() { return panel; }
	
	public void showReceptorFrame(int x, int y) {
		receptorFrame.setLocation(x, y);
		receptorFrame.setVisible(true);
	}
	
	public void showFeedbackFrame(int x, int y) {
		feedbackFrame.setLocation(x, y);
		feedbackFrame.setVisible(true);
	}
	
	public static void main(String args[]) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		AudioProteinCache cache = new AudioProteinCache();
		
		Receptor r = new Receptor();
		r.showReceptorFrame(0, 0);
		r.showFeedbackFrame(0, 0);
		
		ReceptorPlayer p = new ReceptorPlayer(cache);
		r.setPlayer(p);
		
		ArrayListChromosome<Double> a = new ArrayListChromosome<Double>();
		
		ArrayListGene<Double> g1 = new ArrayListGene<Double>();
		g1.add(new DoubleScaleFactor(1.0));
		g1.add(new DoubleScaleFactor(1.0));
		a.add(g1);
		
		ArrayListGene<Double> g2 = new ArrayListGene<Double>();
		g2.add(new DoubleScaleFactor(1.0));
		g2.add(new DoubleScaleFactor(1.0));
		a.add(g2);
		
		BasicDyadicChromosome y = new BasicDyadicChromosome(1.0, 0.99);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(5000, y, cache);
		
		AdjustmentLayerOrganSystem<Long, Double> system = new AdjustmentLayerOrganSystem<Long, Double>(s,
			new CellAdjustmentFactory<Long, Double>() {
				public CellAdjustment<Long, Double> generateAdjustment(double arg) {
					return new PeriodicCellAdjustment(0.012, 2.0, 2.2, cache);
				}
			},
		a);
		
		system.setAdjustmentLayerProteinCache(new FloatingPointProteinCache());
		
		r.getPlayerPanel().addDelayCell(s.getCellA(), 0, 10000);
		r.getPlayerPanel().addDelayCell(s.getCellB(), 0, 10000);
		
		globalMixer = new Mixer(cache, s.getCellA());
		
		SineWaveCell sine = new SineWaveCell(cache);
		sine.setNoteLength(500);
		sine.setAmplitude(0.5);
		sine.setFreq(100);
		sine.setEnvelope(new Envelope() {
			public double getScale(double time) {
				if (time < 0.1)
					return (time / 0.1); // Attenuate the first 10% of audio
				else
					return Math.cos(time * Math.PI / 2);
			}
		});
		
		sine.setReceptor(s.getCellA());
		s.getCellA().setMeter(p);
		
		for (long l = 0; l < Long.MAX_VALUE; l++) {
			sine.push(0);
			s.getCell(0).push(0);
			
			globalMixer.tick();
			system.tick();
		}
		
		p.finish();
	}
	
	public static Mixer getGlobalMixer() { return globalMixer; }
}
