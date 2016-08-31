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
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTable;

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
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.receptor.SamplerPanel;
import com.almostrealism.replicator.geometry.ReplicantScene;
import com.almostrealism.replicator.ui.ReplicatorCanvas;
import com.almostrealism.replicator.ui.ReplicatorTableModel;
import com.almostrealism.synth.SineWaveCell;

public class Replicator {
	public static final boolean enableAudio = false;
	
	private Receptor receptor;
	
	private ReplicatorCanvas canvas;
	private ReplicatorTableModel model;
	
	private JFrame layersFrame, samplerFrame;
	
	public Replicator() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		canvas = new ReplicatorCanvas();
		receptor = new Receptor();
		
		model = new ReplicatorTableModel();
		
		layersFrame = new JFrame("Layers");
		layersFrame.setLayout(new BorderLayout());
		layersFrame.getContentPane().add(new JTable(model));
		layersFrame.setSize(300, 200);
		
		samplerFrame = new JFrame("Sampler");
		samplerFrame.getContentPane().setLayout(new BorderLayout());
		samplerFrame.getContentPane().add(new SamplerPanel(4, 4), BorderLayout.CENTER);
		samplerFrame.setSize(400, 400);
		
		if (enableAudio) {
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
			
			Receptor.globalMixer = new Mixer(cache, s.getCellA());
			
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
				
				Receptor.globalMixer.tick();
				system.tick();
			}
			
			p.finish();
		}
	}
	
	public void addLayer(String name, ShadableSurface s) { model.addLayer(name, s); }
	
	public ReplicatorCanvas getCanvas() { return canvas; }
	
	public ReplicantScene getScene() {
		return new ReplicantScene(model.getSurfaces());
	}
	
	public Action getLayersAction() { return new LayersAction(); }
	public Action getCanvasAction() { return new CanvasAction(); }
	public Action getSamplerAction() { return new SamplerAction(); }
	public Action getFeedbackAction() { return new FeedbackAction(); }
	
	public void showLayersFrame(int x, int y) {
		layersFrame.setLocation(x, y);
		layersFrame.setVisible(true);
	}
	
	public void showSamplerFrame(int x, int y) {
		samplerFrame.setLocation(x, y);
		samplerFrame.setVisible(true);
	}
	
	private class LayersAction extends AbstractAction {
		public LayersAction() {
			super("Layers", new ImageIcon(LayersAction.class.getResource("/icons/L.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { showLayersFrame(0, 0); }
	}
	
	private class CanvasAction extends AbstractAction {
		public CanvasAction() {
			super("Canvas", new ImageIcon(CanvasAction.class.getResource("/icons/V.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { receptor.showReceptorFrame(0, 0); }
	}
	
	private class SamplerAction extends AbstractAction {
		public SamplerAction() {
			super("Sampler", new ImageIcon(SamplerAction.class.getResource("/icons/S.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { showSamplerFrame(0, 0); }
	}
	
	private class FeedbackAction extends AbstractAction {
		public FeedbackAction() {
			super("Feedback", new ImageIcon(FeedbackAction.class.getResource("/icons/F.png")));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) { receptor.showFeedbackFrame(0, 0); }
	}
}
