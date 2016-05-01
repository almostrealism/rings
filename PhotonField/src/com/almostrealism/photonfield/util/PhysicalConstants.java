/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.util;

/**
 * Constants for unit conversion.
 * 
 * @author  Mike Murray
 */
public interface PhysicalConstants {
	/**
	 * Planck's constant in electron volt micro seconds.
	 */
	public static final double H = 4.13500021 * Math.pow(10.0, -9.0);
	
	/**
	 * Speed of light in meters per second. (Same as micrometers per microsecond).
	 */
	public static final double C = 299792458;
	
	/**
	 * The product of the speed of light and Planck's constant.
	 */
	public static final double HC = H * C;
	
	public static final double evMsecToWatts = 1.60217646 * Math.pow(10.0, -13.0);
	public static final double wattsToEvMsec = 1 / evMsecToWatts;
}
