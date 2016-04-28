/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.xml;

import java.awt.Container;

public interface NodeDisplay {
	public Node getNode();
	public Container getContainer();
	public Container getFrame();
	public int getGridWidth();
	public int getGridHeight();
}
