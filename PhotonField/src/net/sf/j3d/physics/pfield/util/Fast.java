package net.sf.j3d.physics.pfield.util;

public interface Fast {
	/**
	 * Sets the time until the next photon should actually be absorbed.
	 * 
	 * @param time  Time until actual absorption (usually in microseconds).
	 */
	public void setAbsorbDelay(double time);
	
	/**
	 * Sets the position of the next photon at the current time (before the
	 * "absorb delay" time specified by the setAbsorbDelay method).
	 * 
	 * @param x  {x, y, z} - Original position of the next photon.
	 */
	public void setOrigPosition(double x[]);
}
