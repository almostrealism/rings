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

package com.almostrealism.feedgrow.population;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.almostrealism.breeding.Genome;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.organs.SimpleOrganFactory;
import org.almostrealism.protein.ProteinCache;

public class SimpleOrganPopulation<T> implements Population<T> {
	private List<Chromosome<Double>> xSomes;
	private List<Chromosome<T>> ySomes;
	private List<SimpleOrgan<T>> organs;
	
	public SimpleOrganPopulation() {
		this(new ArrayList<Chromosome<Double>>(), new ArrayList<Chromosome<T>>());
	}
	
	public SimpleOrganPopulation(List<Genome> g) {
		this(); // Initialize lists using default constructor
		
		Iterator<Genome> itr = g.iterator();
		
		// Extract chromosomes (X and Y) for each organ
		while (itr.hasNext()) {
			Genome c = itr.next();
			
			if (c.size() != 2)
				throw new IllegalArgumentException("Simple organs can only be created with genomes of size 2");
			
			xSomes.add((Chromosome<Double>) c.get(0));
			ySomes.add((Chromosome<T>) c.get(1));
		}
	}
	
	public SimpleOrganPopulation(List<Chromosome<Double>> xSomes, List<Chromosome<T>> ySomes) {
		this.xSomes = xSomes;
		this.ySomes = ySomes;
		this.organs = new ArrayList<SimpleOrgan<T>>();
	}
	
	public void init(SimpleOrganFactory<T> factory) {
		for (int i = 0; i < xSomes.size(); i++) {
			this.organs.add(factory.generateOrgan(xSomes.get(i), ySomes.get(i)));
		}
	}
	
	public void merge(SimpleOrganPopulation<T> pop) {
		for (int i = 0; i < pop.size(); i++) {
			this.xSomes.add(pop.getXChromosome(i));
			this.ySomes.add(pop.getYChromosome(i));
			this.organs.add(pop.getOrgan(i));
		}
	}
	
	public void setProteinCache(ProteinCache<T> cache) {
		Iterator<SimpleOrgan<T>> itr = organs.iterator();
		
		while (itr.hasNext()) {
			itr.next().setProteinCache(cache);
		}
	}

	public Chromosome<Double> getXChromosome(SimpleOrgan<T> o) { return this.xSomes.get(organs.indexOf(o)); }
	public Chromosome<T> getYChromosome(SimpleOrgan<T> o) { return this.ySomes.get(organs.indexOf(o)); }
	public Chromosome<Double> getXChromosome(int index) { return this.xSomes.get(index); }
	public Chromosome<T> getYChromosome(int index) { return this.ySomes.get(index); }
	
	public SimpleOrgan<T> getOrgan(int index) { return this.organs.get(index); }
	
	public int size() { return this.organs.size(); }
	
	public void store(OutputStream s) {
		try (XMLEncoder enc = new XMLEncoder(s)) {	
			for (int i = 0; i < xSomes.size() && i < ySomes.size(); i++) {
				enc.writeObject(xSomes.get(i));
				enc.writeObject(ySomes.get(i));
			}
			
			enc.flush();
		}
	}
	
	public void read(InputStream in) {
		try (XMLDecoder dec = new XMLDecoder(in)) {
			Object read = null;
			
			for (int i = 0; (read = dec.readObject()) != null; i++) {
				if (i % 2 == 0) {
					xSomes.add((Chromosome<Double>) read);
				} else {
					ySomes.add((Chromosome<T>) read);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// End of file
		}
	}
}
