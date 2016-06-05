package com.almostrealism.geometry;

import com.almostrealism.util.graphics.RGB;

public class ColoredGeometry extends BasicGeometry {
	private RGB color;

	public RGB getColor() {
		return color;
	}

	public void setColor(RGB color) {
		this.color = color;
	}
}
