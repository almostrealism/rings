package org.almostrealism.tensorflow;

import io.almostrealism.lang.LanguageOperations;
import org.tensorflow.Operand;
import org.tensorflow.types.TFloat64;

public class TensorFlowConstant extends TensorFlowExpression {
	private final double value;
	private TensorFlowInput input;

	public TensorFlowConstant(double value) {
		this.value = value;
	}

	@Override
	public String getExpression(LanguageOperations lang) {
		return String.valueOf(value);
	}

	@Override
	public Operand<TFloat64> toOperand(TensorFlowManager tf) {
		input = tf.nextInput();
		input.setValue(tf.valueOf(value));
		return input.getInputOperand();
	}
}
