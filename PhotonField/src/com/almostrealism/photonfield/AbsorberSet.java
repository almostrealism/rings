/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield;

import java.util.Iterator;
import java.util.Set;

/**
 * An AbsorberSet instance represents a set of absorbers. An absorber set must keep
 * track of each absorber and provide methods of the Absorber interface that account
 * for the absorption/emission of each absorber in the set.
 * 
 * The contract of the AbsorberSet interface does not require that the
 * implementing class provide the usual acessors of the Set interface
 * return Absorber implementations. It is very likely (as in the case
 * of AbsorberHashSet) that an AbsorberSet implementation extends a
 * standard Set implementation and uses it to store an object which
 * encapsulates an Absorber (AbsorberHashSet.StoredItem).
 * 
 * @author Mike Murray
 */
public interface AbsorberSet extends Absorber, Set {
	/**
	 * Adds the specified absorber to this absorber set.
	 * 
	 * @param a  Absorber instance to add.
	 * @param x  {x, y, z} - Relative position of the absorber.
	 * @return  The total number of absorbers stored by this set.
	 */
	public int addAbsorber(Absorber a, double x[]);
	
	/**
	 * Removes the absorbers contained within the specified spherical volume.
	 * 
	 * @param x  {x, y, z} - The center of the spherical volume.
	 * @param radius  The radius of the spherical volume.
	 * @return  The total number of absorbers removed.
	 */
	public int removeAbsorbers(double x[], double radius);
	
	/**
	 * Removes the specified absorber from this set.
	 * 
	 * @param a  Absorber instance to remove.
	 * @return  The total number of absorbers stored by this set.
	 */
	public int removeAbsorber(Absorber a);
	
	/**
	 * Returns an iterator for the absorbers contained in this absorber set.
	 */
	public Iterator absorberIterator();
	
	/**
	 * @param m  The potential map to use for each absorber in the set.
	 */
	public void setPotentialMap(PotentialMap m);
	
	/**
	 * @return  The potential map used for each absorber in the set.
	 */
	public PotentialMap getPotentialMap();
	
	/**
	 * @param radius  The farthest distance from the origin of a this absorber to a point where
	 *                the absorber has nearly zero likelyhood to absorb a photon. This means
	 *                that photons at a distance greater than this radius from the origin
	 *                of this absorber will not be checked for absorption of a photon. If this
	 *                absorber set is used as the absorber for a photon field, the photon field
	 *                should remove photons with position vectors greater in length than this value.
	 */
	public double getBound();
	
	/**
	 * Returns the largest decimal value that this AbsorberSet can gaurentee is farther
	 * along the direction vector than the first point that absorption may occur. Assuming
	 * that a photon is located at the specified positon vector and traveling along the
	 * specified direction vector, the photon will not be absorbed before traveling the
	 * distance returned by this method.
	 * 
	 * @param p  The position.
	 * @param d  The direction.
	 * @return  The distance before photon may be absorbed.
	 */
	public double getDistance(double p[], double d[]);
	
	/**
	 * @param radius  The farthest distance from the origin of a given absorber in the set
	 *                to a point where the absorber has nearly zero likelyhood to absorb a photon.
	 *                This means that photons at a distance greater than this radius from the origin
	 *                of an absorber in the set will not be checked for absorption of a photon
	 *                If <= 0.0, all absorbers in the set will always be checked for absorption in
	 *                all cases.
	 */
	public void setMaxProximity(double radius);
	
	/**
	 * @return  The farthest distance from the origin of a given absorber in the set
	 *          to a point where the absorber has nearly zero likelyhood to absorb a photon.
	 *          This means that photons at a distance greater than this radius from the origin
	 *          of an absorber in the set will not be checked for absorption of a photon
	 *          If <= 0.0, all absorbers in the set will always be checked for absorption in
	 *          all cases.
	 */
	public double getMaxProximity();
}
