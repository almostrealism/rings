package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;

public class Bin {
	private int key;
	private PackedCollection<Scalar> value;

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public PackedCollection<Scalar> getValue() {
		return value;
	}

	public void setValue(PackedCollection<Scalar> value) {
		this.value = value;
	}
}
