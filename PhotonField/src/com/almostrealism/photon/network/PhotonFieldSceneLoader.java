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

package com.almostrealism.photon.network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import org.almostrealism.flow.Resource;
import org.almostrealism.flow.db.Client;
import org.almostrealism.flow.resources.DistributedResource;
import org.xml.sax.SAXException;

import com.almostrealism.photon.AbsorberHashSet;
import com.almostrealism.photon.AbsorberSet;
import com.almostrealism.photon.raytracer.AbsorberSetRayTracer;
import com.almostrealism.photon.util.FileLoader;
import com.almostrealism.raytracer.Scene;
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
