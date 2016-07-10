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

import com.almostrealism.feedgrow.cellular.CellAdapter;

public abstract class AudioCellAdapter extends CellAdapter<Long> {
	public static int depth = Byte.MAX_VALUE; // AudioProteinCache.depth / 100;
	public static double PI = Math.PI;
	
	public AudioCellAdapter(AudioProteinCache c) {
		setProteinCache(c);
	}
	
	protected int toFrames(int msec) { return (int) ((msec / 1000d) * AudioProteinCache.sampleRate); }
}
