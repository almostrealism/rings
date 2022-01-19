package org.almostrealism.tensorflow;

import io.almostrealism.code.expressions.Expression;
import org.tensorflow.Operand;
import org.tensorflow.types.TFloat64;

import java.util.function.Function;

public class TensorFlowConstant extends TensorFlowExpression {
	private double value;
	private TensorFlowInput input;

	public TensorFlowConstant(double value) {
		this.value = value;
	}

	@Override
	public Operand<TFloat64> toOperand(TensorFlowManager tf) {
		input = tf.nextInput();
		input.setValue(tf.valueOf(value));
		return input.getInputOperand();
	}
}
