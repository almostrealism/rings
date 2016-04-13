package net.sf.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class Sample {
	private boolean stop = true;
	private boolean mute = false;
	private boolean loop = true;
	
	private AudioFormat format;
	
	byte data[][];
	byte empty[];
	double realFFT[];
	
	int pos, loopStart, loopEnd;
	
	public Sample(byte data[][], AudioFormat format) {
		this.data = data;
		this.loopStart = 0;
		this.loopEnd = this.data.length;
		this.empty = new byte[this.data[0].length];
		this.format = format;
	}
	
	public void loadFFT(int n) {
//		this.realFFT = new double[2*n];
//		
//		for (int i = 0; i < realFFT.length; i++) {
//			this.realFFT[i] = ((double) data[i][1]) / 255.0;
//		}
//		
//		DoubleFFT_1D fftd = new DoubleFFT_1D(n);
//		fftd.realForward(this.realFFT);
	}
	
	public Thread playThread(final SourceDataLine line) {
		return new Thread(new Runnable() {
			public void run() {
				play(line);
			}
		});
	}
	
	public synchronized void play(SourceDataLine line) {
		System.out.println(this.toString() + ": Play");
		
		stop = false;
		
		for (pos = loopStart; pos < loopEnd; pos++) {
			if (mute) {
				line.write(empty, 0, empty.length);
			} else {
				line.write(data[pos], 0, data[pos].length);
			}
			
			if (loop && pos + 1 == loopEnd) pos = loopStart - 1;
			if (stop) return;
		}
		
		stop = true;
	}
	
	public void stop() {
		System.out.println(this.toString() + ": Stop");
		stop = true;
	}
	
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
