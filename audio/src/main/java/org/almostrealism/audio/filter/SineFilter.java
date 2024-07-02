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

public class SineFilter implements ByteFunction<byte[]> {
	private int x = 0, len = 100;
	private double scale = 2 * Math.PI / len;
	private double mix = 0.5;

	@Override
	public byte[] operate(byte[] b) {
		if (x == len) x = 0;
		
		byte bx[] = new byte[b.length];
		
		for (int i = 0; i < b.length; i++) {
			bx[i] = (byte) ((1.0 - mix) * b[i] + Math.sin(scale * x) * b[i] * mix);
		}
		
		x++;
		
		return bx;
	}

	@Override
	public Scope<byte[]> getScope(KernelStructureContext context) { throw new RuntimeException("Not implemented"); }
	
	public void setSampleLength(int len) { this.len = len; }
	public void setScale(double scale) { this.scale = scale; }
	public void setMixLevel(double mix) { this.mix = mix; }
	public int getSampleLength() { return this.len; }
	public double getScale() { return this.scale; }
	public double getMixLevel() { return this.mix; }

	@Override
	public String toString() {
		return "SineFilter(" + scale + " " + mix + ")";
	}
}
