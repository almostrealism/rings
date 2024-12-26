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

package org.almostrealism.sound.util;

import java.awt.BorderLayout;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.SourceDataOutputLine;
import org.almostrealism.sound.Sample;
import org.almostrealism.sound.SampleRowColumnDisplay;
import org.almostrealism.sound.SampleRowDisplay;

public class SampleDisplayUtilities {
	public static void showSplit(Sample s) {
		SourceDataLine l = ((SourceDataOutputLine) LineUtilities.getLine(s.getFormat())).getDataLine();
		if (l == null) return;
		l.start();
		SampleRowDisplay sd = new SampleRowDisplay(s.splitLoop(8), l);
		JFrame f = new JFrame("Split");
		f.getContentPane().add(sd);
		f.addKeyListener(sd);
		f.setSize(500, 200);
		f.setVisible(true);
	}
	
	public static void showColumnDisplay(Sample s) {
		SourceDataLine l = ((SourceDataOutputLine) LineUtilities.getLine(s.getFormat())).getDataLine();
		if (l == null) return;
		l.start();
		
		SampleRowColumnDisplay display = new SampleRowColumnDisplay(8);
		display.addSampleRow(s.splitLoop(8), l);
		
		JFrame f = new JFrame("Columns");
		f.setSize(400, 600);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(display, BorderLayout.NORTH);
		f.addKeyListener(display);
		f.setVisible(true);
	}
}
