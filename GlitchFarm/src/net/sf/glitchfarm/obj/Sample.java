package net.sf.glitchfarm.obj;

import javax.sound.sampled.AudioFormat;

import net.sf.glitchfarm.line.OutputLine;
import net.sf.glitchfarm.util.DataProducer;

public class Sample implements DataProducer {
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
	
	public Sample(byte data[][], AudioFormat format) {
		this.data = data;
		this.loopStart = 0;
		this.loopEnd = this.data.length;
		this.empty = new byte[this.data[0].length];
		this.format = format;
	}
	
	/**
	 * Returns a new Sample that references the same underlying data.
	 * BPM information is retained.
	 */
	public Sample shallowCopy() {
		Sample s = new Sample(this.data, this.format);
		s.marker = this.marker;
		s.beatLength = this.beatLength;
		return s;
	}
	
	/**
	 * Returns a new Sample using arraycopy to copy the underlying data
	 * from this sample to the new sample. BPM information is retained.
	 */
	public Sample deepCopy() {
		byte data[][] = new byte[this.data.length][this.data[0].length];
		for (int i = 0; i < data.length; i++) {
			System.arraycopy(this.data[i], 0, data[i], 0, this.data[i].length);
		}
		
		Sample s = new Sample(data, this.format);
		s.marker = this.marker;
		s.beatLength = this.beatLength;
		return s;
	}
	
	public Thread playThread(final OutputLine line) {
		return new Thread(new Runnable() {
			public void run() {
				play(line);
			}
		});
	}
	
	public synchronized void play(OutputLine line) {
		stop = false;
		
		if (fill)
			pos = 0;
		else
			pos = loopStart;
		
		for ( ; pos < loopEnd; pos++) {
			if (mute || pos < loopStart || pos > loopEnd) {
				line.write(empty);
			} else {
				line.write(data[pos]);
			}
			
			if (fill && loop && pos + 1 == this.data.length)
					pos = -1;
			else if (!fill && loop && pos + 1 == loopEnd)
					pos = loopStart - 1;
			
			if (stop) return;
		}
		
		stop = true;
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
	
	public byte[] next() {
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
	public Sample[] splitLoop(int pieces) {
		int totSize = this.loopEnd - this.loopStart;
		if (totSize % pieces != 0)
			totSize += pieces - (totSize % pieces);
		int size = totSize / pieces;
		
		Sample s[] = new Sample[pieces];
		
		for (int i = 0; i < pieces; i++) {
			byte newData[][] = new byte[size][this.data[0].length];
			System.arraycopy(this.data, this.loopStart + i * size, newData, 0, size);
			s[i] = new Sample(newData, this.format);
			s[i].unloop();
		}
		
		return s;
	}
	
	public String toString() { return "Sample[" + this.data.length + ":" + this.hashCode() + "]"; }
}
