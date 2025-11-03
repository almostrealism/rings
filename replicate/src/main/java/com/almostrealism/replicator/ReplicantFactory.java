package com.almostrealism.replicator;

import org.almostrealism.heredity.Chromosome;
import org.almostrealism.color.ShadableSurface;

public interface ReplicantFactory<T, S extends ShadableSurface> {
	Replicant<S> generateReplicant(S surface, Chromosome<T> c);
}
