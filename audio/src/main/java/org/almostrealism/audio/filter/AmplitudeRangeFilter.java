/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.filter;

import io.almostrealism.kernel.KernelStructureContext;
import io.almostrealism.scope.Scope;
import org.almostrealism.graph.ByteFunction;

@Deprecated
public class AmplitudeRangeFilter implements ByteFunction<byte[]> {
	private int min = 0;
	private int max = 128;

	@Override
	public byte[] operate(byte[] b) {
		byte bx[] = new byte[b.length];
		
		for (int i = 0; i < b.length; i++) {
			if (Math.abs(b[i]) < this.min || Math.abs(b[i]) > this.max) {
				bx[i] = 0;
			} else {
				bx[i] = b[i];
			}
		}
		
		return bx;
	}

	@Override
	public Scope<byte[]> getScope(KernelStructureContext context) { throw new RuntimeException("Not implemented"); }

	/** @param min  [0 - 128] */
	public void setMinimumAmplitude(int min) { this.min = min; }
	
	/** @param max  [0 - 128] */
	public void setMaximumAmplitude(int max) { this.max = max; }
	
	public int getMinimumAmplitude() { return this.min; }
	public int getMaximumAmplitude() { return this.max; }
}
