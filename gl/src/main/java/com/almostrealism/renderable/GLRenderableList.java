/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import io.almostrealism.lang.CodePrintWriter;

public class GLRenderableList extends RenderableGLAdapter {
	private RenderableList renderables;
	
	public GLRenderableList() { this(new RenderableList()); }
	
	public GLRenderableList(RenderableList r) { this.renderables = r; }

	public RenderableList getRenderables() { return renderables; }
	
	@Override
	public void init(GLDriver gl) { 
		
		super.init(gl); 
		renderables.init(gl); 
	}
	
	@Override
	public void display(GLDriver gl) {
		
		push(gl);
		super.display(gl);
		renderables.display(gl);
		pop(gl);
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		
		super.write(glMember, name, p);
		renderables.write(glMember, name, p);
	}
}
