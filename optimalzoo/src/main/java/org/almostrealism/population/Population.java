/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.population;

import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Genome;
import org.almostrealism.io.Storable;
import org.almostrealism.organs.Organ;
import org.almostrealism.organs.OrganFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Population<T, O extends Organ<T>> {
	void init(OrganFactory<T, O> factory, Genome templateGenome, Receptor<T> measure);

	void merge(Population<T, O> pop);

	List<Genome> getGenomes();
	
	int size();

	O enableGenome(int index);

	void disableGenome();
}
