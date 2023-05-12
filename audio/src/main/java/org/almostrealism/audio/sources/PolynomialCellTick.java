package org.almostrealism.audio.sources;

/*
 * Copyright 2021 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.expression.Expression;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;

import java.util.Collections;

public class PolynomialCellTick extends PolynomialComputation {
	public PolynomialCellTick(PolynomialCellData data, Producer<Scalar> envelope) {
		super(data, envelope, new Scalar());
	}

	@Override
	public void prepareScope(ScopeInputManager manager) {
		super.prepareScope(manager);

		purgeVariables();

		addVariable(getWavePosition().valueAt(0).assign(
				new Expression<>(Double.class, getWavePosition().valueAt(0).getExpression() +
						" + " + stringForDouble(1.0) + " / " + getWaveLength().valueAt(0).getExpression(),
						Collections.emptyList(),
						getWavePosition(), getWaveLength())));
	}
}
