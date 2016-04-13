/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.raytracer;

import java.io.IOException;

import net.sf.j3d.raytracer.engine.Scene;

public interface SceneLoader {
	public Scene loadScene(String uri) throws IOException;
}
