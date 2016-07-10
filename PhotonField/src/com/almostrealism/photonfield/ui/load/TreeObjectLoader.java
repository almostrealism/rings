/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.ui.load;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.Hashtable;

import com.almostrealism.obj.DefaultObjectFactory;
import com.almostrealism.ui.ObjectTreeDisplay;

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
