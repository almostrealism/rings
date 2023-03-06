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

package org.almostrealism.audioml;

import java.io.File;

public class LocalResourceManager implements GenerationResourceManager, ProcessFeatures {
	private File root;

	public LocalResourceManager(File root) {
		this.root = root;
	}

	@Override
	public void store(String id, File file) {
		run("mv", file.getAbsolutePath(), root.getAbsolutePath() + "/" + id);
	}

	public void load(String id, File dest) {
		run("cp", root.getAbsolutePath() + "/" + id, dest.getAbsolutePath());
	}

	public boolean isAvailable(String id) {
		return new File(root.getAbsolutePath() + "/" + id).exists();
	}
}
