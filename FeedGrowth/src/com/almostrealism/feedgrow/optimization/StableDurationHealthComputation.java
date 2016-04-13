package com.almostrealism.feedgrow.optimization;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.CellAdapter;
import com.almostrealism.feedgrow.metering.AudioMeter;
import com.almostrealism.feedgrow.organ.Organ;

public class StableDurationHealthComputation extends HealthComputationAdapter {
	public static int clipValue = (int) (StrictMath.pow(10, 7));
	
	private AudioProteinCache cache;
	private long max = standardDuration * 3;
	
	public StableDurationHealthComputation(AudioProteinCache cache) {
		this.cache = cache;
	}
	
	public void setMaxDuration(long sec) { this.max = (int) (sec * AudioProteinCache.sampleRate); }
	
	public void setStandardDuration(int sec) {
		this.standardDuration = (int) (sec * AudioProteinCache.sampleRate);
	}
	
	public double computeHealth(Organ<Long> organ) {
		super.init(cache);
		
		AudioMeter meter = getMeter(cache);
		meter.setTextOutputEnabled(false);
		meter.setReportingFrequency(100);
		meter.setClipValue(clipValue);
		
		((CellAdapter<Long>) organ.getCell(0)).setMeter(meter);
		
		setReceptor(organ);
		
		long l;
		
		l: for (l = 0; l < max; l++) {
			push();
			
			// If clipping occurs, report the health score
			if (meter.getClipCount() > 0) break l;
			
			organ.tick();
		}
		
		// Report the health score as a percentage
		// of the expected duration
		return ((double) l) / standardDuration; 
	}
}
