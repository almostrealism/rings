package org.almostrealism.tensorflow.test;

import org.junit.Assert;
import org.junit.Test;
import org.tensorflow.EagerSession;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Constant;
import org.tensorflow.op.math.Sub;
import org.tensorflow.types.TInt32;

public class TensorFlowTest {
	@Test
	public void test() {
		// Allocate a tensor of 32-bits integer of the shape (2, 3, 2)
		TInt32 tensor = TInt32.tensorOf(Shape.of(2, 3, 2));

		// Access tensor memory directly
		Assert.assertEquals(3, tensor.rank());
		Assert.assertEquals(12, tensor.size());

		try (EagerSession session = EagerSession.create()) {
			Ops tf = Ops.create(session);

			// Initialize tensor memory with zeros and take a snapshot
			tensor.scalars().forEach(scalar -> scalar.setInt(0));
			Constant<TInt32> x = tf.constant(tensor);

			// Initialize the same tensor memory with ones and take a snapshot
			tensor.scalars().forEach(scalar -> scalar.setInt(1));
			Constant<TInt32> y = tf.constant(tensor);

			// Subtract y from x and validate the result
			Sub<TInt32> sub = tf.math.sub(x, y);
			sub.asTensor().scalars().forEach(scalar ->
					Assert.assertEquals(-1, scalar.getInt())
			);
		}
	}
}
