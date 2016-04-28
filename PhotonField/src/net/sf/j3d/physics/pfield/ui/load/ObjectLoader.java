/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.ui.load;

import java.awt.Container;
import java.util.Hashtable;

/**
 * @author  Mike Murray
 */
public interface ObjectLoader {
	public Class getParentType();
	public Class[] loadTypes();
	public Hashtable loadOperations();
	public Container getUI();
}
