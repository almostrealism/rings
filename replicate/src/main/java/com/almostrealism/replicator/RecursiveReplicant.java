package com.almostrealism.replicator;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.space.ShadableSurface;

public class RecursiveReplicant<S extends ShadableSurface> extends LayeredReplicant<Replicant<S>> {
	public RecursiveReplicant(ReplicantFactory<Scalar, S> factory, S surface, Chromosome<Scalar> c, int recursion) {
		for (int i = 0; i < recursion; i++) {
			addReplicant(factory.generateReplicant(surface, c));
		}
	}
}
