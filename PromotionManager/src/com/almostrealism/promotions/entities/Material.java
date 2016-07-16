package com.almostrealism.promotions.entities;

import java.util.Hashtable;

/**
 * A {@link Material} instance represents a particular piece of work or
 * artist material which is to be promoted. A variety of meta data can
 * be persisted this way, and analytics services operate on using instances
 * of {@link Material}. This class can be persisted as a java bean using
 * the provided getter and setter methods.
 * 
 * @author  Michael Murray
 */
public class Material {
	/**
	 * Provides a variety of identifiers for particular types of URLs
	 * which refer to the materials contents as the exist across various
	 * web presences including Soundcloud and Beatport.
	 * 
	 * @author  Michael Murray
	 */
	public static enum MaterialURLType {
		Soundcloud, Beatport, Juno, Zippyshare;
	}
	
	/**
	 * Name of this material
	 */
	private String name;
	
	/**
	 * All web presences of this material are collected here, along with their type.
	 */
	private Hashtable<MaterialURLType, String> presence = new Hashtable<MaterialURLType, String>();
	
	/**
	 * Sets the name of this material
	 */
	public void setName(String name) { this.name = name; }
	
	/**
	 * Returns the name of this material
	 */
	public String getName() { return this.name; }
	
	/**
	 * Sets the value returned by {@link #getWebPresences()}
	 */
	public void setWebPresences(Hashtable<MaterialURLType, String> p) { this.presence = p; }
	
	/**
	 * Returns the known web presences for this material. Modifications
	 * to this data structure will side effect this {@link Material} instance.
	 */
	public Hashtable<MaterialURLType, String> getWebPresences() { return presence; }
	
	/**
	 * Returns the name of the material
	 */
	public String toString() { return this.name; }
}
