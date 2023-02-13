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

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.time.Frequency;

import java.util.List;

public class StaticWaveDataProviderFactory implements ParameterizedWaveDataProviderFactory {
	private WaveDataProvider provider;

	public StaticWaveDataProviderFactory() { }

	public StaticWaveDataProviderFactory(WaveDataProvider provider) {
		setProvider(provider);
	}

	public WaveDataProvider getProvider() { return provider; }

	public void setProvider(WaveDataProvider provider) { this.provider = provider; }

	@Override
	public int getCount() { return getProvider().getCount(); }

	@Override
	public WaveDataProviderList create(Producer<Scalar> x, Producer<Scalar> y, Producer<Scalar> z, List<Frequency> playbackRates) {
		// TODO  This should respect the playback rates
		return new WaveDataProviderList(List.of(getProvider()));
	}
}
