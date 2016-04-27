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

package com.almostrealism.raytracer.shaders;

import com.almostrealism.raytracer.engine.AbstractSurface;

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.ColorProducer;
import net.sf.j3d.util.graphics.RGB;

/**
 * @author Mike Murray
 */
public class CheckerBoardShader implements Shader {
  private ColorProducer hotColor, coldColor;

	/**
	 * @see com.almostrealism.raytracer.shaders.Shader#shade(com.almostrealism.raytracer.shaders.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) {
		double intensity = 0.0;
		
		AbstractSurface s = (AbstractSurface) p.getSurface();
		
		Vector v = s.getTransform(true).getInverse().transformAsLocation(p.getPoint());
		
		intensity += Math.sin(v.getX()) + 1;
		intensity += Math.sin(v.getZ()) + 1;
		intensity = intensity / 4.0;
		
		Object args[] = {p};
		
		RGB hot = this.hotColor.evaluate(args);
		hot.multiplyBy(intensity);
		
		RGB cold = this.coldColor.evaluate(args);
		cold.multiplyBy(1.0 - intensity);
		
		// System.out.println(intensity + " " + hot + " " + cold);
		
		hot.addTo(cold);
		
		return hot;
	}

	/**
	 * @see net.sf.j3d.util.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) { return this.shade((ShaderParameters) args[0]); }
	
	/**
	 * @return Returns the coldColor.
	 */
	public ColorProducer getColdColor() { return coldColor; }
	
	/**
	 * @param coldColor The coldColor to set.
	 */
	public void setColdColor(ColorProducer coldColor) { this.coldColor = coldColor; }
	
	/**
	 * @return Returns the hotColor.
	 */
	public ColorProducer getHotColor() { return hotColor; }
	
	/**
	 * @param hotColor The hotColor to set.
	 */
	public void setHotColor(ColorProducer hotColor) { this.hotColor = hotColor; }
}
