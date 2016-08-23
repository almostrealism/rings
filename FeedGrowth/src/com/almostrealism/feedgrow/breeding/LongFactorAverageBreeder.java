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

import com.almostrealism.heredity.ArrayListChromosome;
import com.almostrealism.heredity.ArrayListGene;
import com.almostrealism.heredity.Chromosome;
import com.almostrealism.heredity.Gene;
import com.almostrealism.heredity.LongScaleFactor;

public class LongFactorAverageBreeder implements Breeder<Long> {
	private double mutation = 0.0;
	
	public void setMutationAmount(double m) { this.mutation = m; }
	
	public Chromosome<Long> combine(Chromosome<Long> c1, Chromosome<Long> c2) {
		ArrayListChromosome<Long> chrom = new ArrayListChromosome<Long>();

		for (int i = 0; i < c1.length() && i < c2.length(); i++) {
			Gene<Long> g1 = c1.getGene(i);
			Gene<Long> g2 = c2.getGene(i);
			chrom.add(combine(g1, g2));
		}

		return chrom;
	}

	private Gene<Long> combine(Gene<Long> g1, Gene<Long> g2) {
		ArrayListGene<Long> gene = new ArrayListGene<Long>();
		
		for (int i = 0; i < g1.length() && i < g2.length(); i++) {
			LongScaleFactor f1 = (LongScaleFactor) g1.getFactor(i);
			LongScaleFactor f2 = (LongScaleFactor) g2.getFactor(i);
			
			double scale = (f1.getScale() + f2.getScale()) / 2.0;
			double r = StrictMath.random() - 0.5;
			
			LongScaleFactor factor = new LongScaleFactor(scale + r * mutation);
			gene.add(factor);
		}
		
		return gene;
	}
}
