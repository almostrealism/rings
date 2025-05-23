/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.network;

import io.almostrealism.code.Accessibility;
import io.almostrealism.code.Precision;
import io.almostrealism.lang.DefaultLanguageOperations;
import io.almostrealism.compute.PhysicalScope;

public class JavaScriptLanguageOperations extends DefaultLanguageOperations {
	public JavaScriptLanguageOperations() {
		super(Precision.FP32, false);
	}

	@Override
	public String kernelIndex(int index) { return "0"; }

	@Override
	public String nameForType(Class<?> type) { return ""; }

	@Override
	public String annotationForPhysicalScope(Accessibility access, PhysicalScope scope) { return ""; }

	@Override
	public String pi() {
		return "Math.PI";
	}
}
