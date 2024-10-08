/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.photon.ui.load;

import io.almostrealism.tree.ui.ObjectTreeDisplay;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.almostrealism.obj.DefaultObjectFactory;

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
