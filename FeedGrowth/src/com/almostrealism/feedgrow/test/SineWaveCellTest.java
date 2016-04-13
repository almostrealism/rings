package com.almostrealism.feedgrow.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.sound.sampled.LineUnavailableException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.Envelope;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.audio.WaveOutput;
import com.almostrealism.feedgrow.metering.AudioMeter;
import com.almostrealism.receptor.player.ReceptorPlayer;

public class SineWaveCellTest {
	public static int max = 10 * 1000 * AudioProteinCache.sampleRate; // 100 Seconds
	
	public static void main(String args[]) throws LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
		AudioMeter meter = new AudioMeter(cache);
		meter.setReportingFrequency(100);
		meter.setClipValue(1000000000);
		
		try {
			meter.setForwarding(new WaveOutput(new FileOutputStream("Test.wav"), cache));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		((CellAdapter<Long>) organ.firstCell()).setMeter(meter);
		
		SineWaveCell generator = new SineWaveCell(cache);
		generator.setFreq(880);
		generator.setNoteLength(10000);
		generator.setAmplitude(0.1);
		generator.setEnvelope(new Envelope() {
			public double getScale(double time) {
				if (time < 0.1)
					return (time / 0.1); // Attenuate the first 10% of audio
				else
					return Math.cos(time * Math.PI / 2);
			}
		});
		
		ReceptorPlayer p = new ReceptorPlayer();
		p.setProteinCache(cache);
		
		generator.setReceptor(meter);
		meter.setForwarding(p);
		
		long l;
		
		for (l = 0; l < max; l++) {
			generator.push(0);
		}
		
		p.finish();
	}
}
