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

package com.almostrealism.replicator.geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.almostrealism.space.BasicGeometry;
import org.almostrealism.space.GeometryStack;

import com.almostrealism.raytracer.engine.ShadableSurface;

public class DefaultReplicant<T extends ShadableSurface> extends Replicant<T> {
	private Map<String, BasicGeometry> geo;
	private boolean pushed = false;
	
	public DefaultReplicant(T s) {
		geo = new HashMap<String, BasicGeometry>();
		setGeometry(geometry());
		addSurface(s);
	}
	
	public BasicGeometry get(String name) { return geo.get(name); }
	
	public void put(String name, BasicGeometry g) { geo.put(name, g); }
	
	public Iterable<BasicGeometry> geometry() {
		return () -> { return geo.values().iterator(); };
	}
	
	public void clear() { geo.clear(); }
	
	public Iterator<T> iterator() {
		final Iterator<T> itr = super.iterator();
		
		return new Iterator<T>() {
			private Iterator<BasicGeometry> gitr;
			private T surface;
			
			@Override
			public boolean hasNext() {
				return (itr.hasNext() || (gitr != null && gitr.hasNext()));
			}
			
			@Override
			public T next() {
				if (gitr == null || !gitr.hasNext()) {
					if (surface instanceof GeometryStack) ((GeometryStack) surface).pop();
					pushed = false;
					surface = itr.next();
					gitr = geometry().iterator();
				}
				
				BasicGeometry g = gitr.next();
				
				if (surface instanceof GeometryStack) {
					if (pushed) ((GeometryStack) surface).pop();
					((GeometryStack) surface).push(g);
					pushed = true;
				}
				
				return surface;
			}
		};
	}
}
