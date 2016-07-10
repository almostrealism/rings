/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.obj;

/**
 * @author  Mike Murray
 */
public interface ObjectFactory {
	public Object newInstance() throws InstantiationException, IllegalAccessException;
	public Class getObjectType();
	public Object overlay(Object values[]);
}
