package com.almostrealism.glitchfarm.transform;

import com.almostrealism.glitchfarm.obj.Sample;

/**
 * A SampleTransformer modifies the data of a sample.
 * 
 * @author  Michael Murray (ash)
 */
public interface SampleTransformer {
	public void transform(Sample s);
}
