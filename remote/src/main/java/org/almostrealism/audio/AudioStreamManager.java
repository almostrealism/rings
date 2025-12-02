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

package org.almostrealism.audio;

import org.almostrealism.audio.line.AudioLine;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.DelegatedAudioLine;
import org.almostrealism.audio.line.InputLine;
import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.line.SourceDataOutputLine;
import org.almostrealism.audio.stream.AudioLineDelegationHandler;
import org.almostrealism.audio.stream.AudioServer;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages audio playback channels and their associated {@link BufferedAudioPlayer} instances.
 * This class provides unified infrastructure for both DAW integration mode (streaming to
 * external applications) and direct hardware playback mode.
 * <p>
 * Each channel is identified by a string key and is associated with:
 * <ul>
 *   <li>A {@link BufferedAudioPlayer} that handles audio mixing and playback control</li>
 *   <li>A {@link BufferedOutputScheduler} that manages timing and buffered writes</li>
 *   <li>An output line - either {@link DelegatedAudioLine} for streaming or
 *       {@link SourceDataOutputLine} for direct hardware playback</li>
 * </ul>
 * <p>
 * The manager supports two primary playback modes:
 * <ul>
 *   <li><b>DAW Integration Mode:</b> Use {@link #addPlayer(String, int, OutputLine)} to create
 *       a player with a {@link DelegatedAudioLine} that streams audio to external DAW software
 *       via the {@link AudioServer}.</li>
 *   <li><b>Direct Playback Mode:</b> Use {@link #addDirectPlayer(String, int, OutputLine)} to create
 *       a player with a {@link DelegatedAudioLine} wrapping a {@link SourceDataOutputLine} for direct
 *       hardware playback through the Java Sound API.</li>
 * </ul>
 * <p>
 * Usage example for DAW integration:
 * <pre>{@code
 * AudioStreamManager manager = new AudioStreamManager();
 * manager.start();
 *
 * // Add a player for the "live" channel with 4 player slots
 * BufferedAudioPlayer player = manager.addPlayer("live", 4, recordingLine);
 *
 * // Load audio and control playback
 * player.load(0, "sample.wav");
 * player.play();
 * }</pre>
 *
 * @see BufferedAudioPlayer for the player implementation
 * @see BufferedOutputScheduler for the scheduling mechanism
 * @see AudioServer for the streaming server
 * @see DelegatedAudioLine for streaming/DAW integration output
 * @see SourceDataOutputLine for direct hardware playback output
 */
public class AudioStreamManager implements ConsoleFeatures {
	public static final int PORT = 7799;

	/**
	 * Channel name reserved for direct hardware playback.
	 */
	public static final String DIRECT_CHANNEL = "direct";

	public static double defaultLiveDuration = 180.0;

	private final Map<String, BufferedAudioPlayer> players;
	private final Map<String, BufferedOutputScheduler> schedulers;
	private final Map<String, DelegatedAudioLine> audioLines;
	public AudioServer server;

	public AudioStreamManager() throws IOException {
		this.players = new HashMap<>();
		this.schedulers = new HashMap<>();
		this.audioLines = new HashMap<>();
		this.server = new AudioServer(PORT);
	}

	public void start() throws IOException { server.start(); }

	public AudioServer getServer() { return server; }

	public SampleMixer getMixer(String stream) {
		BufferedAudioPlayer player = getPlayer(stream);
		if (player == null) return null;

		return player.getMixer();
	}

	public BufferedAudioPlayer getPlayer(String channel) {
		return players.get(channel);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 OutputLine inputRecord) {
		DelegatedAudioLine line = new DelegatedAudioLine();
		server.addStream(channel, new AudioLineDelegationHandler(line));
		return addPlayer(channel, playerCount, line, inputRecord);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 AudioLine out, OutputLine inputRecord) {
		int maxFrames = (int) (out.getSampleRate() * defaultLiveDuration);

		// Ensure that the player buffer size is a multiple
		// of the size of the OutputLine buffer
		maxFrames = maxFrames / out.getBufferSize();
		maxFrames *= out.getBufferSize();

		return addPlayer(channel, playerCount, maxFrames, out, inputRecord);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 int maxFrames,
										 AudioLine out, OutputLine inputRecord) {
		if (maxFrames % out.getBufferSize() != 0) {
			throw new IllegalArgumentException();
		}

		BufferedAudioPlayer player = new BufferedAudioPlayer(playerCount, out.getSampleRate(), maxFrames);
		addPlayerScheduler(channel, player, out, inputRecord);
		return player;
	}

	public void addPlayerScheduler(String channel, BufferedAudioPlayer player,
								   AudioLine out, OutputLine inputRecord) {
		if (out instanceof DelegatedAudioLine) {
			audioLines.put(channel, (DelegatedAudioLine) out);
		}
		BufferedOutputScheduler scheduler = addPlayer(channel, player, out, inputRecord);
		schedulers.put(channel, scheduler);
		scheduler.start();
	}

	public BufferedOutputScheduler addPlayer(String channel,
											 BufferedAudioPlayer player,
											 AudioLine out,
											 OutputLine inputRecord) {
		players.put(channel, player);
		return player.deliver(out, inputRecord);
	}

	/**
	 * Adds a player for direct hardware playback using a {@link DelegatedAudioLine}
	 * with a {@link SourceDataOutputLine} as the output delegate.
	 * <p>
	 * This creates a {@link BufferedAudioPlayer} connected directly to the system's
	 * audio output device, bypassing the streaming server. The use of
	 * {@link DelegatedAudioLine} allows future addition of an {@link InputLine} delegate
	 * for audio input support.
	 *
	 * @param channel The channel name (use {@link #DIRECT_CHANNEL} for standard direct playback)
	 * @param playerCount Number of audio sources this player can mix
	 * @param inputRecord Optional output line for recording the mixed output
	 * @return The created BufferedAudioPlayer
	 * @throws IllegalStateException if no audio output line could be obtained
	 */
	public BufferedAudioPlayer addDirectPlayer(String channel, int playerCount,
											   OutputLine inputRecord) {
		OutputLine outputLine = LineUtilities.getLine();
		if (outputLine == null) {
			throw new IllegalStateException("Could not obtain audio output line");
		}

		// Wrap in DelegatedAudioLine for consistency and future input support
		DelegatedAudioLine delegated = new DelegatedAudioLine(
				null,  // No input delegate yet - can be added later
				outputLine,
				outputLine.getBufferSize());

		return addPlayer(channel, playerCount, delegated, inputRecord);
	}

	/**
	 * Gets the scheduler for the specified channel.
	 *
	 * @param channel The channel name
	 * @return The scheduler, or null if no player exists for the channel
	 */
	public BufferedOutputScheduler getScheduler(String channel) {
		return schedulers.get(channel);
	}

	/**
	 * Gets the audio line for the specified channel.
	 *
	 * @param channel The channel name
	 * @return The audio line, or null if no player exists for the channel
	 */
	public DelegatedAudioLine getAudioLine(String channel) {
		return audioLines.get(channel);
	}

	/**
	 * Removes a player and releases its resources.
	 *
	 * @param channel The channel name
	 */
	public void removePlayer(String channel) {
		BufferedOutputScheduler scheduler = schedulers.remove(channel);
		if (scheduler != null) {
			scheduler.stop();
		}
		BufferedAudioPlayer player = players.remove(channel);
		if (player != null) {
			player.destroy();
		}
		// Note: DelegatedAudioLine doesn't own the underlying lines,
		// so we don't destroy it here
		audioLines.remove(channel);
	}

	@Override
	public Console console() { return AudioScene.console; }
}
