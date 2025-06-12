package org.almostrealism.ml.audio;

import org.almostrealism.collect.PackedCollection;

public interface DitModel {
	PackedCollection<?> forward(PackedCollection<?> x, PackedCollection<?> t,
								PackedCollection<?> crossAttnCond,
								PackedCollection<?> globalCond);
}
