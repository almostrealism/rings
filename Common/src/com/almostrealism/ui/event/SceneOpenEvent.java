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

/*
* Copyright (C) 2004  Mike Murray
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

package com.almostrealism.ui.event;

import com.almostrealism.raytracer.engine.*;

/**
  A SceneOpenEvent object represents the event of opening of a new scene.
  It stores the new Scene object and provides access to it.
*/

public class SceneOpenEvent extends SceneEvent {
  private Scene scene;
	
	/**
	  Constructs a new SceneOpenEvent using the specified Scene object.
	*/
	
	public SceneOpenEvent(Scene scene) {
		this.scene = scene;
	}
	
	/**
	  Returns the new Scene object.
	*/
	
	public Scene getScene() {
		return this.scene;
	}
	
	/**
	  Returns "SceneOpenEvent".
	*/
	
	public String toString() {
		return "SceneOpenEvent";
	}
}
