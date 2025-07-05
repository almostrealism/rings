package org.almostrealism.audioml.utils;

import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.types.TFloat32;

import java.util.Random;

public class TensorUtils {
	/**
	 * Creates a tensor filled with random values from a normal distribution.
	 */
	public static TFloat32 createRandomNormalTensor(Shape shape, long seed) {
		Random random = new Random(seed);
		int size = (int)shape.size();
		float[] data = new float[size];

		for (int i = 0; i < size; i++) {
			data[i] = (float)random.nextGaussian();
		}

		return createTensorFromArray(data, shape);
	}

	/**
	 * Creates a TFloat32 tensor from a float array with the specified shape.
	 */
	public static TFloat32 createTensorFromArray(float[] data, Shape shape) {
		return TFloat32.tensorOf(shape, DataBuffers.of(data));
	}

	/**
	 * Converts a TFloat32 tensor to a flat float array.
	 */
	public static float[] tensorToFloatArray(TFloat32 tensor) {
		int size = (int)tensor.shape().size();
		float[] result = new float[size];
		tensor.copyTo(DataBuffers.of(result));
		return result;
	}

	/**
	 * Copies data from one tensor to another.
	 */
	public static void copyTensorData(TFloat32 source, TFloat32 target) {
		if (!source.shape().equals(target.shape())) {
			throw new IllegalArgumentException("Source and target tensors must have the same shape");
		}

		float[] data = tensorToFloatArray(source);
		target.copyFrom(DataBuffers.of(data));
	}

	/**
	 * Prints tensor shape and the first few values for debugging.
	 */
	public static void printTensorInfo(String name, TFloat32 tensor) {
		Shape shape = tensor.shape();
		System.out.println("Tensor " + name + " shape: " + shape);

		if (shape.size() <= 10) {
			float[] data = tensorToFloatArray(tensor);
			System.out.print("Values: [");
			for (int i = 0; i < Math.min(10, data.length); i++) {
				System.out.print(data[i] + (i < Math.min(10, data.length) - 1 ? ", " : ""));
			}
			System.out.println(data.length > 10 ? ", ...]" : "]");
		}
	}
}