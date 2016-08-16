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

package com.almostrealism.raytracer;

import org.almostrealism.space.Scene;

import com.almostrealism.raytracer.engine.RenderParameters;

/**
 * A Project object stores a Scene object and keeps track
 * of rendering options and a home (data) directory.
 * 
 * @author Mike Murray
 */
public class Project {
  private Scene scene;
  private RenderParameters rp;
  
  private String homeDir;

  	/**
  	 * Constructs a new Project object.
  	 */
	public Project() {
		this.scene = null;
		this.rp = new RenderParameters();
		
		this.homeDir = "";
	}
	
	/**
	 * Constructs a new Project object.
	 * 
	 * @param s  The Scene object to be stored by this Project object.
	 */
	public Project(Scene s) {
		this.scene = s;
		this.rp = new RenderParameters();
		
		this.homeDir = "";
	}
	
	/**
	 * Sets the Scene object stored by this Project object.
	 * 
	 * @param s  The Scene object to store.
	 */
	public void setScene(Scene s) { this.scene = s; }
	
	/**
	 * Sets the RenderParameters object stored by this Project object.
	 * 
	 * @param r  The RenderParameters object to store.
	 */
	public void setRenderParameters(RenderParameters r) { this.rp = r; }
	
	/**
	 * Sets the home directory path stored by this Project object.
	 * 
	 * @param dir  The home directory path to store.
	 */
	public void setHomeDir(String dir) { this.homeDir = dir; }
	
	/**
	 * @return  The Scene object stored by this Project object.
	 */
	public Scene getScene() { return this.scene; }
	
	/**
	 * @return  The RenderParameters object stored by this Project object.
	 */
	public RenderParameters getRenderParameters() { return this.rp; }
	
	/**
	 * @return  The home directory path stored by this Project object.
	 */
	public String getHomeDir() { return this.homeDir; }
}