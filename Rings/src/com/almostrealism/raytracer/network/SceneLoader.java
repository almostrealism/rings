/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.raytracer.network;

import java.io.IOException;

import com.almostrealism.raytracer.engine.Scene;

public interface SceneLoader {
	public Scene loadScene(String uri) throws IOException;
}
