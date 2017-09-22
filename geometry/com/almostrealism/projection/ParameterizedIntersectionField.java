/*
 * Copyright 2017 Michael Murray
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

package com.almostrealism.projection;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Ray;
import org.almostrealism.util.ParameterizedFactory;

import java.util.Iterator;

public class ParameterizedIntersectionField implements ParameterizedFactory<Ray, ContinuousField> {
    private Ray ray;
    private Iterator itr;

    public <T extends Ray> void setParameter(Class<T> type, T var) {
        if (type.equals(Ray.class)) {
            this.ray = var;
        } else if (type.equals(Iterator.class)) {
            this.itr = (Iterator) var;
        }
    }

    public ContinuousField construct() {
        return (ContinuousField) Intersections.closestIntersection(ray, itr);
    }
}
