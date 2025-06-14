package org.almostrealism.ml.audio;

import io.almostrealism.profile.OperationProfile;
import org.almostrealism.collect.PackedCollection;

public interface DitModel {
	PackedCollection<?> forward(PackedCollection<?> x, PackedCollection<?> t,
								PackedCollection<?> crossAttnCond,
								PackedCollection<?> globalCond);

	default OperationProfile getProfile() { return null; }
}
