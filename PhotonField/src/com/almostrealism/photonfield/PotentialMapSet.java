/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield;

import java.util.Set;

/**
 * A PotentialMapSet instance represents a set of potential maps that are overlayed (added)
 * to calculate the potential energy for a point in space.
 * 
 * @author Mike Murray
 */
public interface PotentialMapSet extends PotentialMap, Set {
	/**
	 * Adds the specified potential map to this potential map set.
	 * 
	 * @param m  The potential map to add.
	 * @param x  {x, y, z} - The relative position of the potential map.
	 * @return  The total number of potential maps stored by this set.
	 */
	public int addPotentialMap(PotentialMap m, double x[]);
	
	/**
	 * Removes all potential maps with origins within the specified spherical volume.
	 * 
	 * @param x  {x, y, z} - Center of spherical volume.
	 * @param radius  Radius of spherical volume.
	 * @return  The total number of potential maps removed.
	 */
	public int removePotentialMaps(double x[], double radius);
	
	/**
	 * Removes the specified potential map from this set.
	 * 
	 * @param m  Potential map to remove.
	 * @return  The total number of potential maps stored in the set.
	 */
	public int removePotentialMap(PotentialMap m);
	
	/**
	 * @param radius  The farthest distance from the origin of a given potential map in the set
	 *                to a point where the potential map has nearly zero potential. This means
	 *                that points at a distance greater than this radius from the origin of a
	 *                potential map in the set will have the influence of that potential map
	 *                completely ignored. If <= 0.0, all potential maps in the set will always
	 *                be evaluated in all cases.
	 */
	public void setMaxProximity(double radius);
	
	/**
	 * @return  The farthest distance from the origin of a given potential map in the set
	 *          to a point where the potential map has nearly zero potential. This means
	 *          that points at a distance greater than this radius from the origin of a
	 *          potential map in the set will have the influence of that potential map
	 *          completely ignored. If <= 0.0, all potential maps in the set will always
	 *          be evaluated in all cases.
	 */
	public double getMaxProximity();
}
