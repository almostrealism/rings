/*
 * Copyright 2022 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.tensorflow;

import io.almostrealism.scope.Scope;
import org.almostrealism.hardware.AbstractComputeContext;

public class TensorFlowComputeContext extends AbstractComputeContext {
	public TensorFlowComputeContext() {
		super(null, false, false);
	}

	@Override
	public TensorFlowInstructionSet deliver(Scope scope) {
		return new TensorFlowInstructionSet(scope);
	}

	@Override
	public boolean isKernelSupported() { return false; }

	@Override
	public void destroy() {
		// TODO
	}
}