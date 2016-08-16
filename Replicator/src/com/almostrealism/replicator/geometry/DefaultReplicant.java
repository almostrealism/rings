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

import com.almostrealism.raytracer.engine.ShadableSurface;

public class DefaultReplicant extends Replicant implements Iterable<BasicGeometry> {
	private Map<String, BasicGeometry> geo;
	
	public DefaultReplicant(ShadableSurface s) {
		geo = new HashMap<String, BasicGeometry>();
		setGeometry(this);
		setSurface(s);
	}
	
	public BasicGeometry get(String name) { return geo.get(name); }
	
	public void put(String name, BasicGeometry g) { geo.put(name, g); }
	
	public Iterator<BasicGeometry> iterator() { return geo.values().iterator(); }
	
	public void clear() { geo.clear(); }
}
