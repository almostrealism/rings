/*
 * Copyright (C) 2007  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.network.resources;

/**
 * The ResourceHeaderParser interface is implemented by classes that provide
 * methods for identifying if the header (first chunk of bytes from DB) of
 * a DistributedResource indicates that the resource should be represented
 * by a class other than DistributedResource.
 */
public interface ResourceHeaderParser {
	/**
	 * Tests if the resource header indicates that the resource should be
	 * represented by a separate class.
	 * 
	 * @param head  Header for resource.
	 * @return  True if the header matches, false otherwise.
	 */
	public boolean doesHeaderMatch(byte head[]);
	
	/**
	 * Returns the resource class to be used if the header matches.
	 * This must be a subclass of DistributedResource.
	 * 
	 * @return  The resource class to be used.
	 */
	public Class getResourceClass();
}
