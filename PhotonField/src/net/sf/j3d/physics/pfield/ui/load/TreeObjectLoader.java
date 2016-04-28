/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.ui.load;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.Hashtable;

import net.sf.j3d.obj.DefaultObjectFactory;
import net.sf.j3d.physics.pfield.util.VectorMath;
import net.sf.j3d.ui.ObjectTreeDisplay;
import net.sf.j3d.ui.ObjectTreeNode;

/**
 * @author  Mike Murray
 */
public abstract class TreeObjectLoader implements ObjectLoader {
	public static final String overlay = "overlay";
	
	public Container getUI() {
		Class c = this.getParentType();
		Class cl[] = this.loadTypes();
		Hashtable op = this.loadOperations();
		
		DefaultObjectFactory factory = new DefaultObjectFactory(c);
		factory.setOverlayMethod((Method) op.get(TreeObjectLoader.overlay));
		
		ObjectTreeDisplay display = factory.getDisplay();
		for (int i = 0; i < cl.length; i++) display.addObjectType(cl[i]);
		
		return display;
	}
}
