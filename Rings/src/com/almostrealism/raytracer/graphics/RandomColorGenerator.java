/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.graphics;

import com.almostrealism.util.graphics.ColorProducer;
import com.almostrealism.util.graphics.RGB;

/**
 * @author Mike Murray
 */
public class RandomColorGenerator implements ColorProducer {
 private ColorProducer baseRGB, offsetRGB;
 
	public RandomColorGenerator() {
		this(new RGB(0.0, 0.0, 0.0), new RGB(1.0, 1.0, 1.0));
	}
	
	public RandomColorGenerator(ColorProducer baseRGB, ColorProducer offsetRGB) {
		this.baseRGB = baseRGB;
		this.offsetRGB = offsetRGB;
	}
	
	public void setBaseRGB(ColorProducer base) { this.baseRGB = base; }
	public void setOffsetRGB(ColorProducer offset) { this.offsetRGB = offset; }
	
	public ColorProducer getBaseRGB() { return this.baseRGB; }
	public ColorProducer getOffsetRGB() { return this.offsetRGB; }
	
	/**
	 * @see com.almostrealism.raytracer.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) {
		RGB base = this.baseRGB.evaluate(args);
		RGB off = this.offsetRGB.evaluate(args);
		
		base.setRed(base.getRed() + Math.random() * off.getRed());
		base.setGreen(base.getGreen() + Math.random() * off.getGreen());
		base.setBlue(base.getBlue() + Math.random() * off.getBlue());
		
		return base;
	}
}
