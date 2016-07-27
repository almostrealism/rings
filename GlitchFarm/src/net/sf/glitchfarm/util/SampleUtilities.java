package net.sf.glitchfarm.util;

/**
 * Operations using this class currently access static data structures.
 * This means that, for the moment, only one thread may safely execute a
 * method or sequence of methods of this class in one running JVM instance.
 * The data structures used during the operation of this class may be
 * clear by using the static clear() method.
 * 
 * Much of the functionality of this utility class was adapted from a utility written in C++
 * called SoundTouch. Copyright and author information for this is found below. SoundTouch is
 * licensed under the GNU Lesser General Public License.
 * 
 * Author        : Copyright (c) Olli Parviainen
 * Author e-mail : oparviai 'at' iki.fi
 * SoundTouch WWW: http://www.surina.net/soundtouch
 * 
 * @author  Michael Murray (ash)
 */
public class SampleUtilities {
	public static int decimateBy = 5;
	
	public static boolean decayCorrelationBuffer = false;
	public static double correlationDecay = 1.0;
	
	public static int windowStart, windowLen;
	
	protected static float correlationBuffer[];

	/**
	 * low-pass filter & decimate to about 500 Hz. return number of samples output.
	 *
	 * Decimation is used to remove the unnecessary frequencies and thus to reduce
	 * the amount of data needed to be processed for computation intensive activities
	 * such as calculating autocorrelation.
	 *
	 * Anti-alias filtering is done simply by averaging the samples.
	 */
	public static int decimate(int src[], int dest[], int numsamples) {
		int outcount = 0;
		int decimateSum = 0;
		int decimateCount = 0;
		
		double out;
		
		assert(decimateBy != 0);
		
		for (int count = 0; count < numsamples; count++) {
			System.out.println("======Decimate======");
			System.out.println("count: " + count);
			System.out.println("numsamples: " + numsamples);
			System.out.println("outcount: " + outcount);
			System.out.println("decimateSum: " + decimateSum);
			System.out.println("decimateCount:" + decimateCount);
			decimateSum += src[count];
			
			decimateCount++;
			if (decimateCount >= decimateBy) {
				// Store every Nth sample only
				out = (decimateSum / decimateBy);
				decimateSum = 0;
				decimateCount = 0;
				dest[outcount] = (int) out;
				outcount++;
			}
		}
		
		return outcount;
	}
	
	// Calculates autocorrelation function of the sample history buffer
	public static void updateCorrelation(int process_samples) {
	    int offs;
	    int pBuffer[] = new int[0]; // Fix
	    
//	    assert(buffer->numSamples() >= (uint)(process_samples + windowLen));
//
//	    pBuffer = buffer->ptrBegin();
	    for (offs = windowStart; offs < windowLen; offs++) {
	      	double sum;
	        int i;

	        sum = 0;
	        for (i = 0; i < process_samples; i++)
	        {
	            sum += pBuffer[i] * pBuffer[i + offs];    // scaling the sub-result shouldn't be necessary
	        }
	        
	        if (decayCorrelationBuffer) {
		        // decay the correlation buffer here with suitable coefficients
		        // if it's desired that the system adapts automatically to
		        // various bpms, e.g. in processing continuous music stream.
		        // The 'correlationDecay' should be a value that's smaller than but
		        // close to one, and should also depend on 'process_samples' value.
	        	correlationBuffer[offs] *= correlationDecay;
	        }

	       	correlationBuffer[offs] += (float) sum;
	    }
	}
	
	public void clear() {
	}
}
