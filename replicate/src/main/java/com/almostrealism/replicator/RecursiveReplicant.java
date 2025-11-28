package com.almostrealism.replicator;

import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.color.ShadableSurface;

public class RecursiveReplicant<S extends ShadableSurface> extends LayeredReplicant<Replicant<S>> {
	public RecursiveReplicant(ReplicantFactory<PackedCollection, S> factory, S surface, Chromosome<PackedCollection> c, int recursion) {
		for (int i = 0; i < recursion; i++) {
			addReplicant(factory.generateReplicant(surface, c));
		}
	}
}
