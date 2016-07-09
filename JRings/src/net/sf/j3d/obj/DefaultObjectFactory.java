/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.obj;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.almostrealism.ui.ObjectTreeDisplay;
import com.almostrealism.ui.ObjectTreeNode;

/**
 * @author  Mike Murray
 */
public class DefaultObjectFactory implements ObjectFactory {
	private Method localOverlayMethod;
	
	private Class type;
	private Method overlayMethod;
	private Object overlayInvoker;
	
	public DefaultObjectFactory(Class c) {
		this.type = c;
		try {
			this.localOverlayMethod = 
				this.getClass().getMethod("overlay", new Class[] {Object[].class});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Object newInstance() throws InstantiationException, IllegalAccessException {
		return this.type.newInstance();
	}
	
	public void setOverlayMethod(Object invoker, Method m) {
		this.setOverlayMethod(m);
		this.overlayInvoker = invoker;
	}
	
	public void setOverlayMethod(Method m) {
		if (m != null && this.type != null) {
			if (!this.type.isAssignableFrom(m.getReturnType()))
				throw new IllegalArgumentException(
						m.getName() + " does not return " + this.type.getName());
			else if (m.getParameterTypes().length != 1 ||
					m.getParameterTypes()[0].isArray() == false)
				throw new IllegalArgumentException(
						m.getName() + " does not take one parameter.");
			else if (m.getParameterTypes()[0].isArray() == false)
				throw new IllegalArgumentException(
						m.getName() + " does not accept an array.");
		}
		
		this.overlayMethod = m;
	}
	
	public Object overlay(Object values[]) {
		if (this.overlayMethod == null) return null;
		
		Object result = null;
		
		try {
			result = this.overlayMethod.invoke(this.overlayInvoker, new Object[] {values});
		} catch (IllegalAccessException e) {
			System.out.println("DefaultObjectFactory(" + this.type.getName() +
								"): Overlay failed (" + e.getMessage() + ")");
		} catch (InvocationTargetException e) {
			System.out.println("DefaultObjectFactory(" + this.type.getName() +
								"): Overlay failed (" + e.getCause().getMessage() + ")");
		}
		
		return result;
	}
	
	public ObjectTreeDisplay getDisplay() {
		ObjectTreeNode root = new ObjectTreeNode(null, this.localOverlayMethod);
		Class at = this.overlayMethod.getParameterTypes()[0].getComponentType();
		root.setArrayType((Object[]) Array.newInstance(at, 0));
		ObjectTreeDisplay d = new ObjectTreeDisplay(root);
		d.setTarget(this);
		d.addMethodType(this.localOverlayMethod);
		return d;
	}
	
	public Class getObjectType() { return this.type; }
}
