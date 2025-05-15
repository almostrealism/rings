/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.data;

import java.util.Objects;

public class ChannelInfo {
	private int channel;
	private Type type;
	private Voicing voicing;

	public ChannelInfo(int channel) {
		this(channel,  Type.PATTERN, null);
	}

	public ChannelInfo(int channel, Voicing voicing) {
		this(channel, Type.PATTERN, voicing);
	}

	public ChannelInfo(int channel, Type type) {
		this(channel, type, Voicing.MAIN);
	}

	public ChannelInfo(int channel, Type type, Voicing voicing) {
		this.channel = channel;
		this.type = type;
		this.voicing = voicing;
	}

	public int getChannel() { return channel; }

	public Type getType() { return type; }

	public Voicing getVoicing() { return voicing; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChannelInfo that)) return false;
		return getChannel() == that.getChannel() &&
				getType() == that.getType() &&
				getVoicing() == that.getVoicing();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getChannel(), getVoicing());
	}

	public enum Voicing {
		MAIN, WET
	}

	public enum Type {
		PATTERN, RISE
	}
}
