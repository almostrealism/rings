/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.sources;

import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.expression.Expression;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;

import java.util.Collections;

public class SineWavePush extends SineWaveComputation {
	public SineWavePush(SineWaveCellData data, Producer<Scalar> envelope, PackedCollection<?> output) {
		super(data, envelope, output);
	}

	@Override
	public void prepareScope(ScopeInputManager manager) {
		super.prepareScope(manager);

		purgeVariables();

		StringBuilder exp = new StringBuilder();
		exp.append(getEnvelope().valueAt(0).getSimpleExpression());
		exp.append(" * ");
		exp.append(getAmplitude().valueAt(0).getSimpleExpression());
		exp.append(" * ");
		exp.append("sin((");
		exp.append(getWavePosition().valueAt(0).getSimpleExpression());
		exp.append(" + ");
		exp.append(getPhase().valueAt(0).getSimpleExpression());
		exp.append(") * ");
		exp.append(stringForDouble(TWO_PI));
		exp.append(") * ");
		exp.append(getDepth().valueAt(0).getSimpleExpression());

		addVariable(getOutput().valueAt(0).assign(
				new Expression<>(Double.class, exp.toString(), Collections.emptyList(),
						getOutput(), getAmplitude(),
						getWavePosition(), getPhase(),
						getDepth(), getEnvelope())));
	}
}
