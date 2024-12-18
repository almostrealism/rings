/*
 * Copyright 2024 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.line;

import io.almostrealism.util.NumberFormats;

import java.util.function.Consumer;

public class BufferDefaults {
	public static final int groups = 4;
	public static final int batchesPerGroup = 4;
	public static final int batchCount = groups * batchesPerGroup;
	public static final int groupGap = Math.max(1, groups / 2);

	public static int batchSize = 8 * 1024;
	public static double bufferingRate = 2.0;

	public static int defaultBufferSize = batchSize * batchCount;

	public static int getSafeGroup(int currentGroup) {
		int previousGroup = currentGroup - groupGap;
		if (previousGroup < 0) previousGroup += BufferDefaults.groups;
		return previousGroup;
	}

	public static double getGroupDuration(int sampleRate, int totalFrames) {
		int framesPerGroup = totalFrames / groups;
		return framesPerGroup / (double) sampleRate;
	}

	public static void logBufferInfo(int sampleRate, int totalFrames, Consumer<String> out) {
		out.accept("Buffer duration is " + (totalFrames / (double) sampleRate) + "s");
		out.accept("Group duration is " +
				NumberFormats.formatNumber(getGroupDuration(sampleRate, totalFrames)) + "s");
	}
}
