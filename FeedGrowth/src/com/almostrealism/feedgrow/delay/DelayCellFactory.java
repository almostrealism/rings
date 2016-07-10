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

package com.almostrealism.feedgrow.delay;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellFactory;

public class DelayCellFactory implements CellFactory<Long> {
	private int min, delta;
	
	public DelayCellFactory(int minDelay, int maxDelay) {
		this.min = minDelay;
		this.delta = maxDelay - minDelay;
	}
	
	public Cell<Long> generateCell(double arg) {
		return new BasicDelayCell((int) (min + arg * delta));
	}
}
