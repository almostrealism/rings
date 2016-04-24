package com.almostrealism.receptor;

import java.awt.BorderLayout;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.Envelope;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.test.BasicDyadicCellularSystem;
import com.almostrealism.feedgrow.test.BasicDyadicChromosome;
import com.almostrealism.receptor.player.ReceptorPlayer;
import com.almostrealism.receptor.ui.ReceptorPlayerPanel;

public class Receptor {
	private ReceptorPlayerPanel panel;
	
	protected void initUI() {
		panel = new ReceptorPlayerPanel();
		
		JFrame f = new JFrame("Receptor");
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		f.setSize(400, 250);
		f.setVisible(true);
	}
	
	protected void setPlayer(ReceptorPlayer p) { panel.setReceptorPlayer(p); }
	
	public ReceptorPlayerPanel getPlayerPanel() { return panel; }
	
	public static void main(String args[]) throws LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
		Receptor r = new Receptor();
		r.initUI();
		
		ReceptorPlayer p = new ReceptorPlayer(cache);
		r.setPlayer(p);
		
		BasicDyadicChromosome c = new BasicDyadicChromosome(0.95, 1.05);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(1000, c, cache);
		
		r.getPlayerPanel().addDelayCell(s.getCellA(), 0, 10000);
		r.getPlayerPanel().addDelayCell(s.getCellB(), 0, 10000);
		
		SineWaveCell sine = new SineWaveCell(cache);
		sine.setNoteLength(500);
		sine.setAmplitude(0.5);
		sine.setFreq(200);
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
			s.tick();
		}
		
		p.finish();
	}
}
