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

package com.almostrealism.replicator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.almostrealism.geometry.BasicGeometry;
import org.almostrealism.space.GeometryStack;
import org.almostrealism.space.ShadableSurface;

public class DefaultReplicant<T extends ShadableSurface> extends Replicant<T> {
	private Map<String, BasicGeometry> geo;
	
	public DefaultReplicant(T s) {
		geo = new HashMap<>();
		setGeometry(geometry()); // TODO Unnecessary due to overriding the geometry() method?
		addSurface(s);
	}
	
	public BasicGeometry get(String name) { return geo.get(name); }
	
	public void put(String name, BasicGeometry g) { geo.put(name, g); }

	@Override
	public Iterable<BasicGeometry> geometry() {
		return () -> geo.values().iterator();
	}
	
	public void clear() { geo.clear(); }
}
