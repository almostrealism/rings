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

package com.almostrealism.vst;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.almostrealism.cells.Cell;
import org.almostrealism.heredity.DoubleScaleFactor;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.LongScaleFactor;
import org.almostrealism.io.Console;
import org.almostrealism.organs.Organ;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.organs.SimpleOrganFactory;
import org.almostrealism.population.SimpleOrganPopulation;

import com.almostrealism.feedgrow.delay.Delay;

public class PopulationProgramSet extends SimpleOrganPopulation<Long> {
	public static Console console = new Console();
	
	private HashMap<Organ<Long>, String> names = new HashMap<Organ<Long>, String>();
	
	public PopulationProgramSet(String file) {
		try {
			read(new FileInputStream(file));
			console.println("Read chromosome data from " + file);
		} catch (FileNotFoundException e) {
			console.println("Could not load chromosome data");
		}
	}
	
	public int indexOf(Organ<Long> o) {
		for (int i = 0; i < size(); i++) {
			if (o == getOrgan(i)) return i;
		}
		
		return -1;
	}
	
	public void setParameter(Organ<Long> o, int index, float value) {
		Cell<Long> c = null;
		
		int cellIndex = 0;
		
		w: while (true) {
			c = o.getCell(cellIndex);
			
			int newIndex = index - countParams(((SimpleOrgan<Long>) o), cellIndex);
			
			if (newIndex < 0) {
				break w;
			} else {
				index = newIndex;
			}
		}
		
		Gene<Long> g = ((SimpleOrgan<Long>) o).getGene(cellIndex);
		
		if (index < g.length()) {
			Factor<?> f = g.getFactor(index);
			
			if (f instanceof DoubleScaleFactor) {
				((DoubleScaleFactor) f).setScale(value);
			} else if (f instanceof LongScaleFactor) {
				((LongScaleFactor) f).setScale(value);
			}
		} else if (index == g.length() && c instanceof Delay) {
			float delay = SimpleOrganFactory.minDelay + value *
							(SimpleOrganFactory.maxDelay - SimpleOrganFactory.minDelay);
			((Delay) c).setDelay((int) delay);
		}
	}
	
	public float getParameter(Organ<Long> o, int index) {
		Cell<Long> c = null;
		
		int cellIndex = 0;
		
		w: while (true) {
			c = o.getCell(cellIndex);
			
			int newIndex = index - countParams(((SimpleOrgan<Long>) o), cellIndex);
			
			if (newIndex < 0) {
				break w;
			} else {
				index = newIndex;
			}
		}
		
		Gene<Long> g = ((SimpleOrgan<Long>) o).getGene(cellIndex);
		
		if (index < g.length()) {
			Factor<?> f = g.getFactor(index);
			
			if (f instanceof DoubleScaleFactor) {
				return (float) ((DoubleScaleFactor) f).getScale();
			} else if (f instanceof LongScaleFactor) {
				return (float) ((LongScaleFactor) f).getScale();
			}
		} else if (index == g.length() && c instanceof Delay) {
			return ((Delay) c).getDelay();
		}
		
		return 0;
	}
	
	public String getParameterName(Organ<Long> o, int index) {
		Cell<Long> c = null;
		
		int cellIndex = 0;
		
		w: while (true) {
			c = o.getCell(cellIndex);
			
			int newIndex = index - countParams(((SimpleOrgan<Long>) o), cellIndex);
			
			if (newIndex < 0) {
				break w;
			} else {
				index = newIndex;
			}
		}
		
		Gene<Long> g = ((SimpleOrgan<Long>) o).getGene(cellIndex);
		
		if (index < g.length()) {
			return cellIndex + ": Y[" + index + "]";
		} else if (index == g.length() && c instanceof Delay) {
			return cellIndex + ": Delay";
		}
		
		return "";
	}
	
	public String getParameterLabel(Organ<Long> o, int index) {
		Cell<Long> c = null;
		
		int cellIndex = 0;
		
		w: while (true) {
			c = o.getCell(cellIndex);
			
			int newIndex = index - countParams(((SimpleOrgan<Long>) o), cellIndex);
			
			if (newIndex < 0) {
				break w;
			} else {
				index = newIndex;
			}
		}
		
		Gene<Long> g = ((SimpleOrgan<Long>) o).getGene(cellIndex);
		
		if (index < g.length()) {
			return "%";
		} else if (index == g.length() && c instanceof Delay) {
			return "ms";
		}
		
		return "";
	}
	
	public double getParameterMultiplier(Organ<Long> o, int index) {
		Cell<Long> c = null;
		
		int cellIndex = 0;
		
		w: while (true) {
			c = o.getCell(cellIndex);
			
			int newIndex = index - countParams(((SimpleOrgan<Long>) o), cellIndex);
			
			if (newIndex < 0) {
				break w;
			} else {
				index = newIndex;
			}
		}
		
		Gene<Long> g = ((SimpleOrgan<Long>) o).getGene(cellIndex);
		
		if (index < g.length()) {
			return 0.001;
		} else if (index == g.length() && c instanceof Delay) {
			return 1000;
		}
		
		return 1;
	}
	
	public int getParameterCount(Organ<Long> o) {
		if (o instanceof SimpleOrgan) {
			SimpleOrgan<Long> org = (SimpleOrgan<Long>) o;
			
			int params = 0;
			
			for (int i = 0; i < org.size(); i++) {
				params += countParams(org, i);
			}
			
			return params;
		} else {
			return 0;
		}
	}
	
	public void setProgramName(Organ<Long> o, String name) {
		names.remove(o);
		names.put(o, name);
	}
	
	public String getProgramName(Organ<Long> o) { return names.get(o); }
	
	private int countParams(SimpleOrgan<Long> o, int index) {
		// SimpleOrgan has a Y chromosomal parameter for every cell
		int params = o.getGene(index).length();
		
		// Cells which implement delay have a delay
		// time parameter
		if (o.getCell(index) instanceof Delay) params++;
		
		return params;
	}
}
