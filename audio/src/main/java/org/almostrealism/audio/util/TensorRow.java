package org.almostrealism.audio.util;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Tensor;

import java.util.stream.IntStream;

public class TensorRow<T> {
	private Tensor<T> tensor;
	private int row;

	public TensorRow(Tensor<T> tensor, int row) {
		this.tensor = tensor;
		this.row = row;
	}

	public T get(int index) {
		return tensor.get(row, index);
	}

	public void set(int index, T value) {
		tensor.insert(value, row, index);
	}

	public int length() { return tensor.length(row); }

	public void applyLog() {
		for (int i = 0; i < tensor.length(row); i++) {
			Scalar v = (Scalar) tensor.get(row, i);

			if (v.getValue() < 0.0)
				throw new IllegalArgumentException("Trying to take log of a negative number.");

			tensor.insert((T) new Scalar(Math.log(v.getValue())), row, i);
		}
	}

	public void setZero() {
		int size = length();
		for (int i = 0; i < size; i++) set(i, (T) new Scalar(0.0));
	}

	public void addMatVec(Tensor<Scalar> matrix, ScalarBank vector) {
		int m = matrix.length();
		int n = matrix.length(0);
		assert n == vector.getCount();

		for (int i = 0; i < m; i++) {
			double v = 0;

			for (int j = 0; j < n; j++) {
				v += matrix.get(i, j).getValue() * vector.get(j).getValue();
			}

			set(i, (T) new Scalar(v));
		}
	}

	public void mulElements(ScalarBank vals) {
		int size = length();
		assert size == vals.getCount();

		IntStream.range(0, size)
				.forEach(i -> set(i, (T) new Scalar(((Scalar) get(i)).getValue() * vals.get(i).getValue())));
	}
}
