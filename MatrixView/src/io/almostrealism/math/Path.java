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

package io.almostrealism.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Path {
	private List path;
	
	public Path() { this.path = new ArrayList(); }
	
	public void addVertex(Vertex v) {
		Vertex l = this.last();
		
		if (!l.hasPeer(v)) throw new IllegalArgumentException(
							"Vertex " + v + " is not a child of vertex " + l);
		
		this.path.add(v);
	}
	
	public void append(Path p) {
		this.path.addAll(p.path);
	}
	
	public Vertex last() { return (Vertex) this.path.get(this.path.size() - 1); }
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Path [");
		
		Iterator itr = this.path.iterator();
		boolean first = true;
		
		while (itr.hasNext()) {
			if (!first) b.append(", ");
			if (first) first = false;
			b.append(itr.next());
		}
		
		b.append("]");
		
		return b.toString();
	}
}
