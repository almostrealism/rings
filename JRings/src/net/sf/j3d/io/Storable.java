/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.io;

import java.io.OutputStream;

/**
 * An implementation of the Storable interface provides a way to persist the state of
 * an instance. The implementing class must provide a store method that accepts an
 * OutputStream. The implementing class should provide some obvious way to load the
 * state of a stored instance (usually by a static method or constructor).
 * 
 * @author Mike Murray
 */
public interface Storable {
	/**
	 * Persist the contents of this Storable instance.
	 * 
	 * @param out  Stream to write contents to.
	 * @return  True if succesfully stored, false otherwise.
	 */
	public boolean store(OutputStream out);
}
