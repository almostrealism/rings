package org.almostrealism.audio.test;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.WaveData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class WaveDataSlice implements CellFeatures {
	@Test
	public void slice() throws IOException {
		double sliceDuration = bpm(120).l(1);
		WaveData wave = WaveData.load(new File("/Users/michael/Desktop/Cuba.wav"));

		i: for (int i = 0; ; i++) {
			if ((i + 1) * sliceDuration > wave.getDuration()) {
				break i;
			}

			WaveData slice = wave.range(i * sliceDuration, sliceDuration);
			slice.save(new File("/Users/michael/Documents/AudioLibrary/Cuba_" + i + ".wav"));
		}
	}
}
