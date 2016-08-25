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

package org.almostrealism.cells.delay;

import org.almostrealism.cells.Cell;
import org.almostrealism.cells.CellFactory;

import com.almostrealism.audio.filter.BasicDelayCell;

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
