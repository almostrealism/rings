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

package com.almostrealism.gl;

import com.almostrealism.renderable.GLDriver;
import com.almostrealism.renderable.RenderableGLAdapter;

public class DisplayList extends RenderableGLAdapter {
	protected int displayListIndex;
	
	protected DisplayList() { }
	
	public DisplayList(int displayListIndex) {
		this.displayListIndex = displayListIndex;
	}
	
	public void init(GLDriver gl) {
		super.init(gl);
		displayListIndex = gl.glGenLists(1);
	}
	
	@Override
	public void display(GLDriver gl) {
		push(gl);
		super.display(gl);
		gl.glCallList(displayListIndex);
		pop(gl);
	}
}
