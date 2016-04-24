package com.almostrealism.feedgrow.test;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.content.FloatingPointProteinCache;
import com.almostrealism.feedgrow.heredity.ArrayListChromosome;
import com.almostrealism.feedgrow.heredity.ArrayListGene;
import com.almostrealism.feedgrow.heredity.DoubleScaleFactor;
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
