/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import org.xml.sax.SAXException;

import net.sf.j3d.network.Resource;
import net.sf.j3d.network.db.Client;
import net.sf.j3d.network.resources.DistributedResource;
import net.sf.j3d.physics.pfield.AbsorberHashSet;
import net.sf.j3d.physics.pfield.AbsorberSet;
import net.sf.j3d.physics.pfield.raytracer.AbsorberSetRayTracer;
import net.sf.j3d.physics.pfield.util.FileLoader;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.network.SceneLoader;

public class PhotonFieldSceneLoader implements SceneLoader {
	public static boolean local = false;
	private boolean fLocal = false;
	private String uri;
	private int w, h;
	
	private static Hashtable cache = new Hashtable();
	
	public PhotonFieldSceneLoader() { this.fLocal = true; }
	
	public PhotonFieldSceneLoader(String uri, boolean local) {
		this.uri = uri;
		this.fLocal = local;
	}
	
	public static void putCache(String uri, Scene s) {
		PhotonFieldSceneLoader.cache.put(uri, s);
	}
	
	public Scene loadScene() throws FileNotFoundException { return this.loadScene(this.uri); }
	
	public Scene loadScene(String uri) throws FileNotFoundException {
		this.uri = uri;
		
		if (this.cache.containsKey(uri)) return (Scene) this.cache.get(uri);
		
		Resource r = null;
		InputStream in = null;
		
		if (this.fLocal)
			in = new FileInputStream(uri);
		else
			r = Client.getCurrentClient().getServer().loadResource(
											uri, PhotonFieldSceneLoader.local);
		
		AbsorberSet a = null;
		
		try {
			if (in == null && r == null) {
				System.out.println("PhotonFieldSceneLoader: Could not load " + uri);
				throw new IllegalArgumentException("Could not load " + uri);
			} else if (r != null) {
				a = FileLoader.loadSet(r.getInputStream(), this);
			} else if (in != null) {
				a = FileLoader.loadSet(in, this);
			}
		} catch (SAXException e) {
			System.out.println("PhotonFieldSceneLoader: Could not load absorber set (" +
								e.getMessage() + ")");
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.out.println("PhotonFieldSceneLoader: Could not load absorber set (" +
								e.getMessage() + ")");
			throw new RuntimeException(e);
		}
		
		if (a instanceof AbsorberHashSet == false) {
			return null;
		} else {
			AbsorberSetRayTracer tracer = ((AbsorberHashSet)a).getRayTracer();
			this.w = tracer.getWidth();
			this.h = tracer.getHeight();
			return tracer.getScene();
		}
	}
	
	public int getWidth() { return this.w; }
	public int getHeight() { return this.h; }
	
	public String getURI() { return this.uri; }
	
	public InputStream getInputStream(String suffix) throws IOException {
		String base = this.uri.substring(0, this.uri.lastIndexOf("."));
		String resUri = base + suffix;
		
		if (this.fLocal) {
			return new FileInputStream(resUri);
		} else {
			DistributedResource res = (DistributedResource) Client.getCurrentClient()
													.getServer().loadResource(resUri);
			return res.getInputStream();
		}
	}
	
	public OutputStream getOutputStream(String suffix) throws IOException {
		String base = this.uri.substring(0, this.uri.lastIndexOf("."));
		String resUri = base + suffix;
		
		if (this.fLocal)
			return new FileOutputStream(resUri);
		else
			return Client.getCurrentClient().getServer().getOutputStream(resUri);
	}
}
