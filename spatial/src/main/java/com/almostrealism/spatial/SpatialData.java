/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.spatial;

public class SpatialData<T extends SpatialTimeseries> {
	private int index;
	private T timeseries;

	public SpatialData() { }

	public SpatialData(int index, T timeseries) {
		this.index = index;
		this.timeseries = timeseries;
	}

	public int getIndex() { return index; }
	public void setIndex(int index) { this.index = index; }

	public String getKey() { return timeseries == null ? null : timeseries.getKey(); }

	public T getTimeseries() { return timeseries; }
	public void setTimeseries(T timeseries) { this.timeseries = timeseries; }
}
