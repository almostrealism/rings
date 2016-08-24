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

import org.almostrealism.heredity.ArrayListChromosome;
import org.almostrealism.heredity.ArrayListGene;
import org.almostrealism.heredity.DoubleScaleFactor;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.content.FloatingPointProteinCache;
import com.almostrealism.feedgrow.optimization.SimpleOrganOptimizer;
import com.almostrealism.feedgrow.optimization.StableDurationHealthComputation;
import com.almostrealism.feedgrow.systems.AdjustmentLayerOrganSystem;
import com.almostrealism.feedgrow.systems.CellAdjustmentFactory;
import com.almostrealism.feedgrow.systems.PeriodicCellAdjustment;

public class AdjustmentLayerOrganTest {
	private static AudioProteinCache cache = new AudioProteinCache();
	
	public static void main(String args[]) {
		BasicDyadicChromosome y = new BasicDyadicChromosome(0.85, 1.15);
		ArrayListChromosome<Double> a = new ArrayListChromosome<Double>();
		
		ArrayListGene<Double> g1 = new ArrayListGene<Double>();
		g1.add(new DoubleScaleFactor(0.0));
		g1.add(new DoubleScaleFactor(0.0));
		a.add(g1);
		
		ArrayListGene<Double> g2 = new ArrayListGene<Double>();
		g2.add(new DoubleScaleFactor(0.0));
		g2.add(new DoubleScaleFactor(0.0));
		a.add(g2);
		
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(500, y, cache);
		
		AdjustmentLayerOrganSystem<Long, Double> system = new AdjustmentLayerOrganSystem<Long, Double>(s,
				new CellAdjustmentFactory<Long, Double>() {
					public CellAdjustment<Long, Double> generateAdjustment(double arg) {
						return new PeriodicCellAdjustment(0.001, 0.2, 1.0, cache);
					}
				},
		a);
		
		system.setAdjustmentLayerProteinCache(new FloatingPointProteinCache());
		
		StableDurationHealthComputation h = new StableDurationHealthComputation(cache);
		h.setDebugOutputFile("Test.wav");
		double d = h.computeHealth(system);
		
		System.out.println("Health is " + SimpleOrganOptimizer.percent(d));
	}
}
