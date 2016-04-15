package com.almostrealism.feedgrow.audio;

public class SineWaveCell extends AudioCellAdapter {
//	private static int sineWave[];
//	
//	static {
//		sineWave = createSinWaveBuffer(800, 30000);
//	}
	
	private Envelope env;
	
	private double wavePos, wavelength = 1.0;
	private double wavelengthFrames = AudioProteinCache.sampleRate;
	
	private double ampScale = 0.5;
	private double notePos, phase;
	private int note;
	private boolean noteEnded = false;
	
	public SineWaveCell(AudioProteinCache c) { super(c); }
	
	public void strike() { this.notePos = 0; }
	
	public void setFreq(double hertz) {
		this.wavelengthFrames = ((double) AudioProteinCache.sampleRate) / hertz;
		this.wavelength = 1.0 / wavelengthFrames;
	}
	
	public void setPhase(double phase) { this.phase = phase; }
	
	public void setEnvelope(Envelope e) { this.env = e; }
	
	public void setAmplitude(double amp) { this.ampScale = amp; }
	
	public void setNoteLength(int msec) { this.note = toFrames(msec); }
	
	public void push(long index) {
		if (noteEnded) {
			super.push(addProtein(0l));
			return;
		}
		
		if (notePos >= 1.0  && note != 0) {
			noteEnded = true;
			super.push(addProtein(0l));
			return;
		}
		
		double d = ampScale * Math.sin((wavePos + phase) * 2 * PI) * depth;
		if (env != null) d = d * env.getScale(notePos);
		
		long l = addProtein((long) d);
		wavePos += wavelength;
		notePos += 1.0 / note;
		
		super.push(l);
		
//		super.push(addProtein((long) (sineWave[bufIndex++])));
	}
	
	public static int[] createSinWaveBuffer(double freq, int ms) {
		int samples = (int) ((ms * AudioProteinCache.sampleRate) / 1000);
		int[] output = new int[samples];
		
		double period = (double) AudioProteinCache.sampleRate / freq;
		for (int i = 0; i < output.length; i++) {
			double angle = 2.0 * Math.PI * i / period;
			output[i] = (int) (Math.sin(angle) * depth);
		}

		return output;
	}
}
