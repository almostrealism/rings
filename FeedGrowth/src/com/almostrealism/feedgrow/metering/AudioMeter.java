package com.almostrealism.feedgrow.metering;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;

public class AudioMeter implements Receptor<Long> {
	private ProteinCache<Long> cache;
	private Receptor<Long> forwarding;
	private int freq = 1;
	private int count = 0;
	
	private long clipCount;
	private long clipValue = Long.MAX_VALUE;
	
	private long silenceValue = 0;
	private long silenceDuration;
	
	private boolean outEnabled = true;
	
	public AudioMeter(ProteinCache<Long> cache) { setProteinCache(cache); }
	
	public void setTextOutputEnabled(boolean enabled) { this.outEnabled = enabled; }
	
	public void setReportingFrequency(int msec) {
		this.freq = (int) ((msec / 1000d) * AudioProteinCache.sampleRate);
	}
	
	public void setProteinCache(ProteinCache<Long> p) { this.cache = p; }
	
	public ProteinCache<Long> getProteinCache() { return this.cache; }
	
	public void setForwarding(Receptor<Long> r) { this.forwarding = r; }
	
	public void setSilenceValue(long value) { this.silenceValue = value; }
	
	public long getSilenceDuration() { return this.silenceDuration; }
	
	public void setClipValue(long value) { this.clipValue = value; }
	
	public long getClipCount() { return clipCount; }
	
	public void push(long l) {
		if (outEnabled) {
			if (count == 0) {
				System.out.println(cache.getProtein(l));
			} else if (cache.getProtein(l) != 0) {
				System.out.println(cache.getProtein(l) + " [Frame " + count + " of " + freq + "]");
			}
		}
		
		count++;
		count = count % freq;
		
		if (cache.getProtein(l) >= clipValue) clipCount++;
		if (cache.getProtein(l) > silenceValue) silenceDuration = 0;
		if (cache.getProtein(l) <= silenceValue) silenceDuration++;
		
		if (forwarding != null) forwarding.push(l);
	}
}
