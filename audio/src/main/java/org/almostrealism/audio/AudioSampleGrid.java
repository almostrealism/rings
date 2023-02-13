package org.almostrealism.audio;

/**
 * An {@link AudioSampleGrid} is a collection of four {@link AudioSample}s which
 * are queued/played in order A1, B1, A2, B2. Since {@link AudioSampleGrid} is
 * itself an {@link AudioSample}, grids cells can be subdivided to produce more
 * complex patterns.
 */
// TODO  Generate from a chromosome
//       One set of genes will determine the probability of subdividing
//       in each of the four cells, while another set of genes determines
//       the probability of a particular sample appearing in a particular
//       cell for a given grid depth.
public class AudioSampleGrid extends AudioSample {
	private AudioSample a1, b1;
	private AudioSample a2, b2;

	public AudioSample getPositionA1() { return a1; }
	public AudioSample getPositionB1() { return b1; }
	public AudioSample getPositionA2() { return a2; }
	public AudioSample getPositionBb() { return b2; }
}
