/*
 * Copyright (C) 2004-05  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package net.sf.j3d.run;

import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.engine.Scene;

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