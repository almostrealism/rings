package org.almostrealism.color;

import java.util.ArrayList;
import java.util.Arrays;

import org.almostrealism.uml.Function;

@Function
public class ColorProduct extends ArrayList<ColorProducer> implements ColorProducer {
	public ColorProduct() { }
	public ColorProduct(ColorProducer... producers) { addAll(Arrays.asList(producers)); }
	
	@Override
	public RGB evaluate(Object[] args) {
		RGB rgb = new RGB();
		for (ColorProducer c : this) { rgb.multiplyBy(c.evaluate(args)); }
		return rgb;
	}
}
