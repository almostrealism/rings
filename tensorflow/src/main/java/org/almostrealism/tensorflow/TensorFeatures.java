package org.almostrealism.tensorflow;

import org.tensorflow.Operand;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;

public interface TensorFeatures {
	default TInt32 matrix(Shape shape, int[][] values) {
		if (!shape.isMatrix()) throw new UnsupportedOperationException();

		TInt32 matrix = TInt32.tensorOf(shape);
		matrix.scalars().forEachIndexed((coords, scalar) -> {
			int i = (int) coords[0];
			int j = (int) coords[1];
			scalar.setInt(values[i][j]);
		});

		return matrix;
	}

	default Operand<TInt32> constantVector(int... values) {
		return getTensorFlow().constant(vector(values));
	}

	default TInt32 vector(int... values) {
		return vector(Shape.of(values.length), values);
	}

	default TInt32 vector(Shape shape, int[] values) {
		if (!shape.isVector()) throw new UnsupportedOperationException();

		TInt32 vector = TInt32.tensorOf(shape);
		vector.scalars().forEachIndexed((coords, scalar) -> {
			int i = (int) coords[0];
			scalar.setInt(values[i]);
		});

		return vector;
	}

	default Operand<TFloat32> reshape(Operand<TFloat32> input, Shape shape) {
		long newShape[] = shape.asArray();
		int s[] = new int[newShape.length];
		for (int i = 0; i < s.length; i++) {
			if (newShape[i] >= Integer.MAX_VALUE) throw new UnsupportedOperationException();
			s[i] = (int) newShape[i];
		}

		return getTensorFlow().reshape(input, constantVector(s));
	}

	Ops getTensorFlow();
}
