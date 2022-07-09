package org.almostrealism.wavenet;

import org.almostrealism.tensorflow.TensorFeatures;
import org.tensorflow.Operand;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;

import java.util.List;

public class Operations implements TensorFeatures {
	private Ops tf;

	public Operations(Ops tf) {
		this.tf = tf;
	}

	@Override
	public Ops getTensorFlow() {
		return tf;
	}

	public Operand<TFloat32> timeToBatch(Operand<TFloat32> value, long dilation) {
		Shape shape = value.shape();
		long padElements = dilation - 1 - (shape.size(1) + dilation - 1) % dilation;
		TInt32 paddings = matrix(Shape.of(3, 2), new int[][] { {0, 0}, { 0, (int) padElements }, { 0, 0 } });

		Operand<TFloat32> padded = tf.pad(value, tf.constant(paddings), tf.constant(TFloat32.tensorOf(Shape.scalar())));
		Operand<TFloat32> reshaped = tf.reshape(padded, tf.constant(vector(-1, (int) dilation, (int) shape.size(2))));
		Operand<TFloat32> transposed = tf.linalg.transpose(reshaped, tf.constant(vector(1, 0, 2)));
		return tf.reshape(transposed, tf.constant(vector((int) shape.size(0) * (int) dilation, -1, (int) shape.size(2))));
	}

	public Operand<TFloat32> batchToTime(Operand<TFloat32> value, long dilation) {
		Shape shape = value.shape();
		Operand<TFloat32> prepared = reshape(value, Shape.of(dilation, -1, shape.size(2)));
		Operand<TFloat32> transposed = tf.linalg.transpose(prepared, constantVector(1, 0, 2));
		return reshape(transposed, Shape.of(shape.size(0) / dilation, -1, shape.size(2)));
	}

	public Operand<TFloat32> causalConvolution(Operand<TFloat32> value, Operand<TFloat32> filter, long dilation) {
		long filterWidth = filter.shape().asArray()[0];

		Operand<TFloat32> transformed;
		Operand<TFloat32> conv;
		Operand<TFloat32> restored;

		if (dilation > 1) {
			transformed = timeToBatch(value, dilation);
			conv = conv1d(transformed, filter, 1l, "VALID");
			restored = batchToTime(conv, dilation);
		} else {
			restored = conv1d(value, filter, 1l, "VALID");
		}

		long outWidth = value.shape().asArray()[1] - (filterWidth - 1) * dilation;
		Operand<TFloat32> result = tf.slice(restored, constantVector(0, 0, 0), constantVector(-1, (int) outWidth, -1));
		return result;
	}

	public Operand<TFloat32> conv1d(Operand<TFloat32> input, Operand<TFloat32> filter, Long stride, String padding) {
		return conv1d(input, filter, 1l, stride, 1l, padding);
	}

	public Operand<TFloat32> conv1d(Operand<TFloat32> input, Operand<TFloat32> filter, Long nStride, Long wStride, Long cStride, String padding) {
		long inputShape[] = input.shape().asArray();

		long newInputShape[] = new long[inputShape.length + 1];
		for (int i = 0; i < newInputShape.length - 3; i++) {
			newInputShape[i] = inputShape[i];
		}

		newInputShape[newInputShape.length - 3] = 1;
		newInputShape[newInputShape.length - 2] = inputShape[inputShape.length - 2];
		newInputShape[newInputShape.length - 1] = inputShape[inputShape.length - 1];

		Operand<TFloat32> reshapedInput = reshape(input, Shape.of(newInputShape));
		Operand<TFloat32> reshapedFilter = reshape(filter, filter.shape().prepend(1));

		Operand<TFloat32> conv = tf.nn.conv2d(reshapedInput, reshapedFilter, List.of(nStride, wStride, wStride, cStride), padding);
		long outputShape[] = conv.shape().asArray();
//		System.out.println("Original conv shape: " + Arrays.toString(outputShape));

		long newOutputShape[] = new long[outputShape.length - 1];
		for (int i = 0; i < newInputShape.length - 3; i++) {
			newOutputShape[i] = outputShape[i];
		}

		newOutputShape[newOutputShape.length - 2] = outputShape[outputShape.length - 2];
		newOutputShape[newOutputShape.length - 1] = outputShape[outputShape.length - 1];
//		System.out.println("New conv shape: " + Arrays.toString(newOutputShape));
		return reshape(conv, Shape.of(newOutputShape));
	}
}
