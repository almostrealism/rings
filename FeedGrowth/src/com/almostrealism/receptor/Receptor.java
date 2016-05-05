package com.almostrealism.receptor;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.Envelope;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.content.FloatingPointProteinCache;
import com.almostrealism.feedgrow.heredity.ArrayListChromosome;
import com.almostrealism.feedgrow.heredity.ArrayListGene;
import com.almostrealism.feedgrow.heredity.DoubleScaleFactor;
import com.almostrealism.feedgrow.systems.AdjustmentLayerOrganSystem;
import com.almostrealism.feedgrow.systems.CellAdjustmentFactory;
import com.almostrealism.feedgrow.systems.PeriodicCellAdjustment;
import com.almostrealism.feedgrow.test.BasicDyadicCellularSystem;
import com.almostrealism.feedgrow.test.BasicDyadicChromosome;
import com.almostrealism.receptor.mixing.Mixer;
import com.almostrealism.receptor.player.ReceptorPlayer;
import com.almostrealism.receptor.ui.ReceptorPlayerPanel;
import com.almostrealism.receptor.ui.SamplerPanel;
import com.almostrealism.visualize.ui.ReceptorCanvas;

public class Receptor {
	private static Mixer globalMixer;
	
	private ReceptorCanvas canvas;
	private ReceptorPlayerPanel panel;
	
	protected void initUI() throws UnsupportedAudioFileException, IOException {
		canvas = new ReceptorCanvas();
		panel = new ReceptorPlayerPanel();
		
		JFrame r = new JFrame("Receptor");
		r.getContentPane().setLayout(new BorderLayout());
		r.getContentPane().add(canvas, BorderLayout.CENTER);
		r.setSize(400, 400);
		r.setLocationRelativeTo(null);
		r.setLocation(r.getLocation().x, r.getLocation().y - 200);
		r.setVisible(true);
		
		JFrame f = new JFrame("Feedback");
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		f.setSize(400, 250);
		f.setLocation(r.getLocation().x, r.getLocation().y + r.getHeight());
		f.setVisible(true);
		
		JFrame s = new JFrame("Sampler");
		s.getContentPane().setLayout(new BorderLayout());
		s.getContentPane().add(new SamplerPanel(4, 4), BorderLayout.CENTER);
		s.setSize(400, 400);
		s.setLocation(r.getLocation().x + r.getWidth(), r.getLocation().y);
		s.setVisible(true);
		
		canvas.start();
	}
	
	protected void setPlayer(ReceptorPlayer p) { panel.setReceptorPlayer(p); }
	
	public ReceptorPlayerPanel getPlayerPanel() { return panel; }
	
	public static void main(String args[]) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		AudioProteinCache cache = new AudioProteinCache();
		
		Receptor r = new Receptor();
		r.initUI();
		
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
