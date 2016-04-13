/*
 * Copyright (C) 2007  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.raytracer.loaders;

import java.io.IOException;

import net.sf.j3d.io.FileDecoder;
import net.sf.j3d.network.db.Client;
import net.sf.j3d.raytracer.SceneFactory;
import net.sf.j3d.raytracer.SceneLoader;
import net.sf.j3d.raytracer.engine.Scene;

/**
 * The PlySceneLoader loads a PLY model from the distributed database
 * and places it in a default scene.
 * 
 * @author  Mike Murray
 */
public class GtsSceneLoader implements SceneLoader {
	public static final double scale = 100.0;
	
	// TODO  Add use of exception listener.
	/**
	 * @see net.sf.j3d.raytracer.SceneLoader#loadScene(java.lang.String)
	 */
	public Scene loadScene(String uri) throws IOException {
		Scene scene = FileDecoder.decodeScene(Client.getCurrentClient().getServer().loadResource(uri),
												FileDecoder.GTSEncoding, null);
		scene.setLights(SceneFactory.getStandard3PointLightRig(scale));
		return scene;
	}
}