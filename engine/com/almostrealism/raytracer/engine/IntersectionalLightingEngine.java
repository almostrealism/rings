package com.almostrealism.raytracer.engine;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Intersections;
import org.almostrealism.algebra.Ray;
import org.almostrealism.color.ColorProducer;
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
