package org.almostrealism.audio.filter;

import org.almostrealism.audio.JavaAudioSample;
import org.almostrealism.audio.SampleTransformer;

/**
 * {@link BinaryTransformer} divides the sample into a specified number of pieces
 * and then turns each piece into a mask (solid block of 0 or 127). The value
 * will be zero if the average magnitude of the subsample is less than a
 * specified threshold, and 127 otherwise.
 * 
 * The BinaryTransformer derives averages from only bytes 2 and 4 of a 4
 * byte sample, however the derived sample will have all channels set to
 * either 127 or 0.
 */
public class BinaryTransformer implements SampleTransformer<JavaAudioSample> {
	private int pieces = 8;
	private double thresh = 64;
	private byte one[] = { 127, 127, 127, 127 };
	private byte zero[] = { 0, 0, 0, 0 };
	
	/**
	 * Sets the number of pieces to break the sample into.
	 */
	public void setNumberOfPieces(int pieces) { this.pieces = pieces; }
	
	/**
	 * Returns the number of pieces to break the sample into.
	 */
	public int getNumberOfPieces() { return this.pieces; }
	
	/**
	 * Sets the threshold for a piece of sample data to qualify as
	 * an "on" mask.
	 */
	public void setThreshold(double t) { this.thresh = t; }
	
	/**
	 * Returns the threshold for a piece of sample data to qualify as
	 * an "on" mask.
	 */
	public double getThreshold() { return this.thresh; }
	
	public void transform(JavaAudioSample s) {
		int totSize = s.data.length;
		if (totSize % pieces != 0)
			totSize += pieces - (totSize % pieces);
		int size = totSize / pieces;
		
		int index = 0;
		
		for (int i = 0; i < pieces; i++) {
			double avg = 0;
			
			int start = index;
			
			j: for (int j = 0; j < size; j++) {
				if (index >= s.data.length)
					break j;
				avg += Math.abs(s.data[index][1]) + Math.abs(s.data[index][3]);
				index++;
			}
			
			int end = index;
			if (i == pieces - 1)
				end = s.data.length;
			
			avg = avg / (2 * size);
			
			byte value[] = one;
			if (avg <  thresh) value = zero;
			
			for (int j = start; j < end; j++)
				System.arraycopy(value, 0, s.data[j], 0, 4);
		}
	}
	
	public String toString() {
		return "BinaryTransformer";
	}
}
