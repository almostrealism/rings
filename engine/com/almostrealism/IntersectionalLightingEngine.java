/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Intersections;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.util.ParameterizedFactory;

import java.util.concurrent.Callable;

public class IntersectionalLightingEngine extends LightingEngine {
    public IntersectionalLightingEngine(Iterable<? extends Callable<ColorProducer>> allSurfaces) {
        super(new ParameterizedFactory<Ray, ContinuousField>() {
            private Ray r;

            @Override
            public <T extends Ray> void setParameter(Class<T> aClass, T a) {
                this.r = a;
            }

            @Override
            public ContinuousField construct() {
                return (ShadableIntersection) Intersections.closestIntersection(r,
                                    Intersections.filterIntersectables(allSurfaces));
            }
        });
    }
}
