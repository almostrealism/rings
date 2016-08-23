package com.almostrealism.glitchfarm.line;

public interface DualOutputLine {
	public void setMix(double m);
	public double getMix();
	
	public OutputLine getLeftLine();
	public OutputLine getRightLine();
}
