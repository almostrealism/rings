package org.almostrealism.wavenet;

import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.types.TInt32;

public class WavenetApp {
	public static boolean fastGeneration = true;

	public static void main(String args[]) {
		WavenetModel net = new WavenetModel();

	}

	public void run(Ops tf) {
		WavenetModel net = new WavenetModel();

		Placeholder<TInt32> samples = tf.placeholder(TInt32.class);

		if (fastGeneration) {
			// next_sample = net.predictProbaIncremental(samples, args.gc_id);
		} else {
			// next_sample = net.predictProba(samples, args.gc_id);
			throw new UnsupportedOperationException();
		}
	}
}
