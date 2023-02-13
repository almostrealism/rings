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

package org.almostrealism.audio.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;

import java.util.HashMap;
import java.util.Map;

public abstract class WaveDataProviderAdapter implements WaveDataProvider {
	private static Map<String, ContextSpecific<WaveData>> loaded;

	static {
		loaded = new HashMap<>();
	}

	public abstract String getKey();

	protected void clearKey(String key) {
		loaded.remove(key);
	}

	protected abstract WaveData load();

	protected void unload() { clearKey(getKey()); }

	@JsonIgnore
	@Override
	public WaveData get() {
		if (loaded.get(getKey()) == null) {
			loaded.put(getKey(), new DefaultContextSpecific<>(this::load));
			loaded.get(getKey()).init();
		}

		return loaded.get(getKey()).getValue();
	}
}
