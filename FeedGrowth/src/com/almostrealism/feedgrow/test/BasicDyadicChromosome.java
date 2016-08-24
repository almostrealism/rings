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

package com.almostrealism.feedgrow.test;

import java.util.List;

import org.almostrealism.breeding.Breedable;
import org.almostrealism.breeding.Breeder;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.LongScaleFactor;

public class BasicDyadicChromosome implements Chromosome<Long> {
	private Factor<Long> factorA, factorB;
	private Gene<Long> geneA, geneB;
	
	public BasicDyadicChromosome(double scaleA, double scaleB) {
		this.factorA = new LongScaleFactor(scaleA);
		this.factorB = new LongScaleFactor(scaleB);
		
		// Gene A sends to Cell index 1 by factor B
		this.geneA = new Gene<Long>() {
			public Factor<Long> getFactor(int index) {
				return index == 1 ? factorB : null;
			}
			
			public int length() { return 2; }
		};
		
		// Gene B sends to Cell index 0 by factor A
		this.geneB = new Gene<Long>() {
			public Factor<Long> getFactor(int index) {
				return index == 0 ? factorA : null;
			}
			
			public int length() { return 2; }
		};
	}
	
	public Gene<Long> getGene(int index) {
		if (index == 0) {
			return geneA;
		} else if (index == 1) {
			return geneB;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public int length() { return 2; }
	
	public Breedable breed(Breedable b, List<Breeder> l) {
		return l.get(0).combine(this, (Chromosome) b);
	}
}
