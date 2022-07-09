package org.almostrealism.wavenet.test;

import org.almostrealism.wavenet.Operations;
import org.junit.Test;
import org.tensorflow.EagerSession;
import org.tensorflow.Operand;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TFloat32;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationsTest {
	@Test
	public void testCasualConvolution() {
		try (EagerSession session = EagerSession.create()) {
			Ops tf = Ops.create(session);
			Operations ops = new Operations(tf);

			AtomicInteger idx = new AtomicInteger(0);

			TFloat32 x = TFloat32.tensorOf(Shape.of(2, 20, 1));
			x.scalars().forEach(f -> {
				int i = idx.getAndIncrement();
				f.setFloat(i % 20 + 1);
			});

			TFloat32 f = TFloat32.tensorOf(Shape.of(2, 1, 1));
			f.scalars().forEach(v -> v.setFloat(1));

			Operand<TFloat32> out = ops.causalConvolution(tf.constant(x), tf.constant(f), 4);

			out.asTensor().scalars().forEachIndexed((coords, scalar) ->
					System.out.println("Scalar at " + Arrays.toString(coords) + " has value " + scalar.getFloat()));
		}
	}
}
