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

package com.almostrealism.renderable;

import java.util.ArrayList;

import com.almostrealism.gl.GLDriver;
import io.almostrealism.code.CodePrintWriter;

public class RenderableList extends ArrayList<Renderable> implements Renderable {
	@Override
	public void init(GLDriver gl) {
		for (Renderable r : this) r.init(gl);
	}
	
	@Override
	public void display(GLDriver gl) {
		for (Renderable r : this) r.display(gl);
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		int index = 0;
		for (Renderable r : this) r.write(glMember, name + (index++), p);
	}
}
