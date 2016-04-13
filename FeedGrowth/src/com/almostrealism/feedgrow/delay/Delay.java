package com.almostrealism.feedgrow.delay;

public interface Delay {
	public void setDelay(int msec);
	public int getDelay();
	
	public void setDelayInFrames(long frames);
	public long getDelayInFrames();
}
