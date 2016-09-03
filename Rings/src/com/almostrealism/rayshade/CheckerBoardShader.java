/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.rayshade;

import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Vector;

import com.almostrealism.raytracer.engine.AbstractSurface;

/**
 * @author Mike Murray
 */
public class CheckerBoardShader implements Shader {
  private ColorProducer hotColor, coldColor;

	/**
	 * @see com.almostrealism.rayshade.Shader#shade(com.almostrealism.rayshade.ShaderParameters)
	 */
	public ColorProducer shade(ShaderParameters p) {
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
