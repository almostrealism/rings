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

package com.almostrealism.receptor;

import java.awt.GridLayout;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import com.almostrealism.synth.SampleFactory;

public class SamplerPanel extends JPanel {
	public SamplerPanel(int w, int h) throws UnsupportedAudioFileException, IOException {
		super(new GridLayout(h, w));
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
//				add(new SamplePad(SampleFactory.createSample("/Users/mike/Downloads/KICKS/FE_MD_KICK.wav")));
				add(new SamplePad(SampleFactory.createSample(700, 500)));
			}
		}
	}
}
