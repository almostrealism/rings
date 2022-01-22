package org.almostrealism.tensorflow;

import io.almostrealism.expression.Expression;
import org.tensorflow.Operand;
import org.tensorflow.types.TFloat64;

public abstract class TensorFlowExpression extends Expression<Double> {
	public TensorFlowExpression() {
		super(Double.class);
	}

	public abstract Operand<TFloat64> toOperand(TensorFlowManager tf);
}
