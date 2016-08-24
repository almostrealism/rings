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

package org.almostrealism.optimize;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.CellAdapter;
import com.almostrealism.feedgrow.metering.AudioMeter;
import com.almostrealism.feedgrow.organ.Organ;

/**
 * The {@link StableDurationHealthComputation} is a {@link HealthComputationAdapter} which
 * computes a health score based on the duration that an {@link Organ} can be used before
 * a clip value is reached.
 * 
 * @see  #clipValue
 * 
 * @author  Michael Murray
 */
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
