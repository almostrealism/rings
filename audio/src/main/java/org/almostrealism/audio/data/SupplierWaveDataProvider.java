/*
 * Copyright 2023 Michael Murray
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

import org.almostrealism.collect.PackedCollection;

import java.util.function.Supplier;

public class SupplierWaveDataProvider extends WaveDataProviderAdapter {
	private String key;
	private Supplier<PackedCollection<?>> source;
	private int sampleRate;

	public SupplierWaveDataProvider() { }

	public SupplierWaveDataProvider(Supplier<PackedCollection<?>> source, int sampleRate) {
		this.source = source;
		this.sampleRate = sampleRate;
	}

	@Override
	public int getCount() {
		return get().getCollection().getCount();
	}

	@Override
	public double getDuration() {
		return get().getDuration();
	}

	@Override
	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	protected WaveData load() {
		return new WaveData(source.get(), sampleRate);
	}
}
