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

package com.almostrealism.raytracer.ui;

import com.almostrealism.raytracer.engine.Surface;

/**
  A SurfaceRemoveEvent object represents the event of removing a Surface object from the current Scene object.
*/

public class SurfaceRemoveEvent extends SceneEditEvent implements SurfaceEvent {
  private Surface target;

	/**
	  Constructs a new SurfaceRemoveEvent object using the specified target.
	*/
	
	public SurfaceRemoveEvent(Surface target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this SurfaceRemoveEvent object.
	*/
	
	public Surface getTarget() {
		return this.target;
	}
	
	/**
	  Returns "SurfaceRemoveEvent".
	*/
	
	public String toString() {
		return "SurfaceRemoveEvent";
	}
}
