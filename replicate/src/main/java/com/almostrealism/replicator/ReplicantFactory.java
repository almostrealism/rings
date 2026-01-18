package com.almostrealism.replicator;

import org.almostrealism.color.ShadableSurface;
import org.almostrealism.heredity.Chromosome;

public interface ReplicantFactory<T, S extends ShadableSurface> {
	Replicant<S> generateReplicant(S surface, Chromosome<T> c);
}
