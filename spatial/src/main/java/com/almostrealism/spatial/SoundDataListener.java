/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import java.util.List;
import java.util.function.Consumer;

public interface SoundDataListener {
	default void updatedChannels(List<String> channelNames) { }

	void selected(SoundData d);

	void published(int index, SoundData d);

	default void setPlayMode(PlayMode mode) { }

	default void play() { }

	default void pause() { }

	default void seek(double time) { }

	default void togglePlay() { }

	enum PlayMode {
		ALL, SELECTED
	}

	static SoundDataListener onSelected(Consumer<SoundData> consumer) {
		return new SoundDataListener() {
			@Override
			public void selected(SoundData d) { consumer.accept(d); }

			@Override
			public void published(int index, SoundData d) { }
		};
	}
}
