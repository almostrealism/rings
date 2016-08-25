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

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.audio.delay.BasicDelayCell;

public class DelaySlider extends JSlider implements ChangeListener {
	private BasicDelayCell delay;
	
	public DelaySlider(BasicDelayCell d, int orientation, int min, int max) {
		super(orientation, min, max, d.getDelay());
		this.delay = d;
		
		addChangeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) { delay.setDelay(getValue()); }
}
