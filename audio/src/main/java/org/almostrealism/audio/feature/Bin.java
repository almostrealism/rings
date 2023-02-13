package org.almostrealism.audio.feature;

import org.almostrealism.algebra.ScalarBank;

public class Bin {
	private int key;
	private ScalarBank value;

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public ScalarBank getValue() {
		return value;
	}

	public void setValue(ScalarBank value) {
		this.value = value;
	}
}
