package com.almostrealism.feedgrow.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.optimization.SilenceDurationHealthComputation;
import com.almostrealism.feedgrow.optimization.SimpleOrganOptimizer;
import com.almostrealism.feedgrow.optimization.StableDurationHealthComputation;
import com.almostrealism.feedgrow.organ.SimpleOrganFactory;
import com.almostrealism.feedgrow.population.SimpleOrganPopulation;

public class HealthTestTool {
	public static boolean enableSilenceDurationCalculation = false;
	
	public static void main(String args[]) throws FileNotFoundException {
		String file = "Population.xml";
		if (args.length > 0) file = args[0];
		
		AudioProteinCache cache = new AudioProteinCache();
		
		// TODO  Optionally load organ factory from another XML file
		
		SimpleOrganPopulation<Long> pop = new SimpleOrganPopulation<Long>();
		pop.read(new FileInputStream(file));
		pop.init(SimpleOrganFactory.defaultFactory);
		pop.setProteinCache(cache);
		
		StableDurationHealthComputation health1 = new StableDurationHealthComputation(cache);
		SilenceDurationHealthComputation health2 = new SilenceDurationHealthComputation(cache);
		
		for (int i = 0; i < pop.size(); i++) {
			health1.setDebugOutputFile("HealthTest-" + i + ".wav");
			
			double h1 = health1.computeHealth(pop.getOrgan(i));
			System.out.println("Stable Duration Health [Cell " + i + "]: " + SimpleOrganOptimizer.percent(h1));
			
			if (enableSilenceDurationCalculation) {
				double h2 = health2.computeHealth(pop.getOrgan(i));
				System.out.println("Silence Duration Health [Cell " + i + "]: " + SimpleOrganOptimizer.percent(h2));
			}
		}
	}
}
