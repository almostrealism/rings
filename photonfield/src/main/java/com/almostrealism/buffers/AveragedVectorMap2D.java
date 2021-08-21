/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.buffers;

import org.almostrealism.algebra.Vector;
import io.almostrealism.relation.Evaluable;

/**
 * @author  Mike Murray
 */
public interface AveragedVectorMap2D {
	void addVector(double u, double v, Evaluable<Vector> e, boolean front);
	double[] getVector(double u, double v, boolean front);
	int getSampleCount(double u, double v, boolean front);
}
