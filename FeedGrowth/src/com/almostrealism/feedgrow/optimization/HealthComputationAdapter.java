package com.almostrealism.feedgrow.optimization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.Envelope;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.audio.WaveOutput;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.metering.AudioMeter;

public abstract class HealthComputationAdapter implements HealthComputation<Long> {
	protected int standardDuration = (int) (100 * AudioProteinCache.sampleRate);
	
	public static int frequency = (int) 391.95; // G
	
	private SineWaveCell generator;
	
	private String debugFile;
	
	protected void init(AudioProteinCache cache) {
		generator = new SineWaveCell(cache);
		generator.setFreq(frequency);
		generator.setNoteLength(190);
		generator.setAmplitude(0.30);
		generator.setEnvelope(new Envelope() {
			public double getScale(double time) {
				if (time < 0.1)
					return (time / 0.1); // Attenuate the first 10% of audio
				else
					return Math.cos(time * Math.PI / 2);
			}
		});
	}
	
	protected AudioMeter getMeter(AudioProteinCache cache) {
		AudioMeter meter = new AudioMeter(cache);
		
		if (debugFile != null) {
			try {
				meter.setForwarding(new WaveOutput(new FileOutputStream(debugFile), cache));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return meter;
	}
	
	protected void push() { generator.push(0); }
	
	protected void setReceptor(Receptor<Long> r) { generator.setReceptor(r); }
	
	public void setDebugOutputFile(String file) { this.debugFile = file; }
}
