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

package org.almostrealism.organs;

import org.almostrealism.cells.Cell;
import org.almostrealism.cells.CellAdjustment;
import org.almostrealism.cells.Receptor;
import org.almostrealism.protein.ProteinCache;

import com.almostrealism.audio.delay.BasicDelayCell;
import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.synth.SineWaveCell;


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
		generator.setAmplitude(10);
		generator.setReceptor(this);
	}
	
	public void adjust(Cell<Long> toAdjust, Double arg) {
		generator.push(0);
		long frames = (long) ((min + factor * (max - min)) * AudioProteinCache.sampleRate);
//		long frames = (long) factor * ((BasicDelayCell) toAdjust).getDelay();
		((BasicDelayCell) toAdjust).setDelayInFrames(frames);
	}
	
	public void push(long proteinIndex) {
		this.factor = (generator.depth + cache.getProtein(proteinIndex)) / ((double) generator.depth);
	}
}
