/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.health;

import org.almostrealism.audio.AudioMeter;
import org.almostrealism.audio.OutputLine;

import java.util.ArrayList;
import java.util.List;

public class SilenceDurationHealthComputation extends HealthComputationAdapter {
	public static boolean enableVerbose = false;
	public static boolean enableSilenceCheck = false;
	
	private int maxSilence;
	private double silenceValue = 0.001; // Lowest permissable volume

	private long max = standardDuration;

	private List<Runnable> silenceListeners;
	
	public SilenceDurationHealthComputation(int channels) {
		this(channels, 2);
	}
	

	public SilenceDurationHealthComputation(int channels, int maxSilenceSec) {
		super(channels);
		setMaxSilence(maxSilenceSec);
		silenceListeners = new ArrayList<>();
	}
	
	public void setMaxSilence(int sec) { this.maxSilence = (int) (sec * OutputLine.sampleRate); }
	
	public static void setStandardDuration(int sec) {
		standardDuration = (int) (sec * OutputLine.sampleRate);
	}

	public void addSilenceListener(Runnable listener) { silenceListeners.add(listener); }

	@Override
	protected void configureMeasures(List<AudioMeter> measures) {
		measures.forEach(m -> m.setSilenceValue(silenceValue));
	}

	public boolean checkForSilence(AudioMeter meter) {
		if (enableSilenceCheck) {
			if (meter.getSilenceDuration() > maxSilence) {
				silenceListeners.forEach(Runnable::run);
				return true;
			}
		}

		return false;
	}

	@Override
	public AudioHealthScore computeHealth() {
		long l;

		// Runnable push = organ.push(null).get();
		Runnable tick = getTarget().tick().get();
		
		l: for (l = 0; l < max; l++) {
			// push.run();

			for (AudioMeter m : getMeasures()) {
				// If silence occurs for too long, report the health score
				if (checkForSilence(m)) {
					return new AudioHealthScore((double) l / standardDuration);
				}
			}
			
			tick.run();
		}
		
		// Report the health score as an inverse
		// percentage of the expected duration
		if (enableVerbose)
			System.out.println("SilenceDurationHealthComputation: " + l + " frames of survival");
		
		// If no silence which was too long in duration
		// has occurred, return a perfect health score.
		return new AudioHealthScore(1.0);
	}
}
