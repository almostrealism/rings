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

package com.almostrealism.feedgrow.breeding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.almostrealism.feedgrow.heredity.Chromosome;

public class Genome extends ArrayList<Chromosome<?>> implements Breedable {
	public Genome() { this(new ArrayList<Chromosome<?>>()); }
	
	public Genome(List<Chromosome<?>> somes) { addAll(somes); }
	
	public Genome getHeadSubset() {
		Genome subset = new Genome();
		Iterator<Chromosome<?>> itr = iterator();
		
		while (itr.hasNext()) {
			Chromosome c = itr.next();
			if (itr.hasNext()) subset.add(c);
		}
		
		return subset;
	}
	
	public Chromosome getLastChromosome() { return get(size() - 1); }
	
	public Breedable breed(Breedable b, List<Breeder> l) {
		if (b instanceof Collection == false)
			throw new IllegalArgumentException("Invalid type for breeding");
		
		Iterator<Chromosome<?>> citr = this.iterator();
		Iterator<Chromosome<?>> bitr = ((Collection)b).iterator();
		Iterator<Breeder> ditr = l.iterator();
		
		Genome g = new Genome();
		
		while (citr.hasNext() && bitr.hasNext() && ditr.hasNext()) {
			g.add(ditr.next().combine(citr.next(), bitr.next()));
		}
		
		return g;
	}
}
