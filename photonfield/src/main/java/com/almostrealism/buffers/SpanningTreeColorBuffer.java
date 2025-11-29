/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.buffers;

import io.almostrealism.relation.Factory;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShadableSurface;
import org.almostrealism.space.Scene;

import java.io.IOException;

/**
 * @author  Michael Murray
 */
public class SpanningTreeColorBuffer implements ColorBuffer {
	private double m = 1.0;
	
	public void addColor(double u, double v, boolean front, RGB c) {
		// TODO Auto-generated method stub
	}
	
	public RGB getColorAt(double u, double v, boolean front) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void clear() {
		// TODO Auto-generated method stub	
	}
	
	public double getScale() { return this.m; }
	public void setScale(double m) { this.m = m; }
	
	public void store(Factory<Scene<ShadableSurface>> loader, String name) throws IOException {
		// TODO Auto-generated method stub
	}
	
	public void load(Factory<Scene<ShadableSurface>> loader, String name) throws IOException {
		// TODO Auto-generated method stub
	}
}