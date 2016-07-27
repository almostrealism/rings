package net.sf.glitchfarm.transform;

import net.sf.glitchfarm.obj.Sample;

/**
 * A SampleTransformer modifies the data of a sample.
 * 
 * @author  Michael Murray (ash)
 */
public interface SampleTransformer {
	public void transform(Sample s);
}
