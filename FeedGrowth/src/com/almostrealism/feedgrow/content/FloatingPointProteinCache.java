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

package com.almostrealism.feedgrow.content;

public class FloatingPointProteinCache implements ProteinCache<Double> {
	public static int addWait = 0;
	
	public static int sampleRate = 44100;
	public static int depth = (int) StrictMath.pow(2, 32);
	public static int bufferDuration = 100;
	
	private double data[] = new double[sampleRate * bufferDuration];
	private int cursor;
	
	public long addProtein(Double p) {
		tryWait();
		data[cursor] = p == null ? 0.0 : p;
		cursor++;
		
		long index = cursor - 1;
		cursor = cursor % data.length;
		return index;
	}
	
	public Double getProtein(long index) { return data[(int) index]; }
	
	private void tryWait() {
		if (addWait == 0) return;
		if (cursor % sampleRate != 0) return;
		
		try {
			Thread.sleep(addWait);
		} catch (InterruptedException e) { }
	}
}
