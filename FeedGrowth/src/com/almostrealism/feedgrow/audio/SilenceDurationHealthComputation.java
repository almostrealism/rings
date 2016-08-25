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

package com.almostrealism.feedgrow.audio;

import org.almostrealism.cells.CellAdapter;
import org.almostrealism.organs.Organ;

import com.almostrealism.feedgrow.metering.AudioMeter;

public class SilenceDurationHealthComputation extends HealthComputationAdapter {
	public static boolean enableVerbose = false;
	
	private int maxSilence = (int) (0.005 * AudioProteinCache.sampleRate); // 2 seconds
	private int silenceValue = 100; // Lowest permissable volume
	private double scale = 1000;
	
	private AudioProteinCache cache;
	private long max = standardDuration;
	
	public SilenceDurationHealthComputation(AudioProteinCache cache) {
		this.cache = cache;
	}
	

	public SilenceDurationHealthComputation(AudioProteinCache cache, int maxSilenceSec) {
		this.cache = cache;
		setMaxSilence(maxSilenceSec);
	}
	
	public void setMaxSilence(int sec) { this.maxSilence = (int) (sec * AudioProteinCache.sampleRate); }
	
	public void setStandardDuration(int sec) {
		this.standardDuration = (int) (sec * AudioProteinCache.sampleRate);
	}
	
	public double computeHealth(Organ<Long> organ) {
		super.init(cache);
		
		AudioMeter meter = getMeter(cache);
		meter.setTextOutputEnabled(false);
		meter.setReportingFrequency(100);
		meter.setSilenceValue(silenceValue);
		
		((CellAdapter<Long>) organ.getCell(0)).setMeter(meter);
		
		setReceptor(organ);
		
		long l;
		
		l: for (l = 0; l < max; l++) {
			push();
			
			// If silence occurs for too long, report the health score
			if (meter.getSilenceDuration() > maxSilence) {
				return Math.max(0.0, 1.0 - ((((double) l) / standardDuration) * scale));
			}
			
			organ.tick();
		}
		
		// Report the health score as an inverse
		// percentage of the expected duration
		if (enableVerbose)
			System.out.println("SilenceDurationHealthComputation: " + l + " frames of survival");
		
		// If no silence which was too long in duration
		// has occurred, return a perfect health score.
		return 1.0;
	}
}
