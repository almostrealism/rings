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

import javax.media.opengl.GL2;

import com.almostrealism.renderable.RenderableList;

public class RenderableGLList extends RenderableGLAdapter {
	private RenderableList renderables;
	
	public RenderableGLList() { this(new RenderableList()); }
	
	public RenderableGLList(RenderableList r) { this.renderables = r; }
	
	@Override
	public void init(GL2 gl) { super.init(gl); renderables.init(gl); }
	
	@Override
	public void display(GL2 gl) {
		push(gl);
		renderables.display(gl);
		pop(gl);
	}
}