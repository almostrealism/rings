package org.almostrealism.audio;

import javax.sound.sampled.AudioFormat;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.line.OutputLine;

/**
 * @deprecated This class uses the old byte[] approach and does not support
 * Producer-based buffered writes with BufferedOutputScheduler. Use WaveData
 * and WaveCell instead for modern audio playback.
 */
@Deprecated
public class JavaAudioSample extends AudioSample implements Evaluable<byte[]> {
	private boolean stop = true;
	private boolean mute = false;
	private boolean loop = true;
	private boolean fill = false;
	
	private AudioFormat format;
	
	public byte data[][];
	byte empty[];
	double realFFT[];
	
	public int pos, loopStart, loopEnd;
	public int marker = -1, beatLength = -1;
	
	public JavaAudioSample(byte data[][], AudioFormat format) {
		this.data = data;
		this.loopStart = 0;
		this.loopEnd = this.data.length;
		this.empty = new byte[this.data[0].length];
		this.format = format;
	}
	
	/**
	 * Returns a new JavaAudioSample that references the same underlying data.
	 * BPM information is retained.
	 */
	public JavaAudioSample shallowCopy() {
		JavaAudioSample s = new JavaAudioSample(this.data, this.format);
		s.marker = this.marker;
		s.beatLength = this.beatLength;
		return s;
	}
	
	/**
	 * Returns a new JavaAudioSample using arraycopy to copy the underlying data
	 * from this sample to the new sample. BPM information is retained.
	 */
	public JavaAudioSample deepCopy() {
		byte data[][] = new byte[this.data.length][this.data[0].length];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(this.data[i], 0, data[i], 0, this.data[i].length);
		}
		
		JavaAudioSample s = new JavaAudioSample(data, this.format);
		s.marker = this.marker;
		s.beatLength = this.beatLength;
		return s;
	}
	
	/**
	 * @deprecated Use BufferedOutputScheduler with WaveCell instead
	 */
	@Deprecated
	public Thread playThread(final OutputLine line) {
		throw new UnsupportedOperationException("Use BufferedOutputScheduler with WaveCell instead");
	}

	/**
	 * @deprecated Use BufferedOutputScheduler with WaveCell instead
	 */
	@Deprecated
	public synchronized void play(OutputLine line) {
		throw new UnsupportedOperationException("Use BufferedOutputScheduler with WaveCell instead");
	}
	
	public void stop() { stop = true; }
	public void restart() { this.pos = this.loopStart; }
	
	public AudioFormat getFormat() { return this.format; }
	
	public boolean isStopped() { return stop; }
	
	public void mute() { this.mute = true; }
	public void unmute() { this.mute = false; }
	public boolean toggleMute() { return this.mute = !this.mute; }
	public boolean isMuted() { return this.mute; }
	
	public void loop() { this.loop = true; }
	public void unloop() { this.loop = false; }
	public boolean toggleLoop() { return this.loop = !this.loop; }
	public boolean isLooped() { return this.loop; }
	public void resetLoop() {
		this.loopStart = 0;
		this.loopEnd = this.data.length;
	}
	
	public void fill() { this.fill = true; }
	public void unfill() { this.fill = false; }
	public boolean toggleFill() { return this.fill = !this.fill; }
	public boolean isFilled() { return this.fill; }
	
	public void setBPM(int marker, int beatLength) {
		this.marker = marker;
		this.beatLength = beatLength;
	}
	
	/**
	 * Calculates BPM by the formula 60.0 / (beat length / fps).
	 */
	public double getBPM() {
		double fps = this.format.getFrameRate();
		return 60.0 / (beatLength / fps);
	}
	
	/**
	 * Returns true if a marker and beat length have been set for this
	 * sample, false otherwise.
	 */
	public boolean isBpmSet() { return marker > -1; }
	
	@Override
	public byte[] evaluate(Object[] args) {
		if (mute || pos < loopStart || pos > loopEnd) {
			return empty;
		}
		
		if (fill && loop && pos + 1 == this.data.length) {
			pos = -1;
		} else if (!fill && loop && pos + 1 == loopEnd) {
			pos = loopStart;
		} else if (pos >= loopEnd) {
			pos = loopStart;
			return null;
		}
		
		return data[pos++];
	}
	
	/**
	 * Splits the loop of this sample evenly into the specified number of smaller samples.
	 * If the number of pieces does not evenly divide the loop length, the loop will be extended
	 * to evenly divided the number of pieces. By default, these subsamples will be unlooped.
	 * 
	 * @param pieces  Number of new samples to create.
	 * @return  Array of subsamples.
	 */
	public JavaAudioSample[] splitLoop(int pieces) {
		int totSize = this.loopEnd - this.loopStart;
		if (totSize % pieces != 0)
			totSize += pieces - (totSize % pieces);
		int size = totSize / pieces;
		
		JavaAudioSample s[] = new JavaAudioSample[pieces];
		
		for (int i = 0; i < pieces; i++) {
			byte newData[][] = new byte[size][this.data[0].length];
			System.arraycopy(this.data, this.loopStart + i * size, newData, 0, size);
			s[i] = new JavaAudioSample(newData, this.format);
			s[i].unloop();
		}
		
		return s;
	}
	
	public String toString() { return "JavaAudioSample[" + this.data.length + ":" + this.hashCode() + "]"; }
}
