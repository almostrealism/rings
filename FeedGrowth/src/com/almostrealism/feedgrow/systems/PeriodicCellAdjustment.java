package com.almostrealism.feedgrow.systems;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.delay.BasicDelayCell;


public class PeriodicCellAdjustment implements CellAdjustment<Long, Double>, Receptor<Long> {
	private AudioProteinCache cache;
	private SineWaveCell generator;
	private double min, max;
	private double factor = 1.0;
	
	public PeriodicCellAdjustment(double freq, double min, double max, ProteinCache<Long> cache) {
		this.min = min;
		this.max = max;
		setProteinCache(cache);
		setFrequency(freq);
	}
	
	public void setProteinCache(ProteinCache<Long> cache) {
		this.cache = (AudioProteinCache) cache;
	}
	
	public void setFrequency(double freq) {
		generator = new SineWaveCell(cache);
		generator.setFreq(freq);
		generator.setPhase(0.5);
		generator.setNoteLength(0);
		generator.setAmplitude(0.1);
		generator.setReceptor(this);
	}
	
	public void adjust(Cell<Long> toAdjust, Double arg) {
		generator.push(0);
		long frames = (long) ((min + factor * (max - min)) * AudioProteinCache.sampleRate);
		if (StrictMath.random() < 0.001) System.out.println("Setting frames to " + frames);
		((BasicDelayCell) toAdjust).setDelayInFrames(frames);
	}
	
	public void push(long proteinIndex) {
		this.factor = cache.getProtein(proteinIndex) / ((double) generator.depth);
	}
}
