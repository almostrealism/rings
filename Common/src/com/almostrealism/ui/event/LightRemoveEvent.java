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

package com.almostrealism.ui.event;

import com.almostrealism.raytracer.lighting.*;

/**
  A LightRemoveEvent object represents the event of removing a Light object from the current Scene object.
*/

public class LightRemoveEvent extends SceneEditEvent implements LightEvent {
  private Light target;

	/**
	  Constructs a new LightRemoveEvent object using the specified target.
	*/
	
	public LightRemoveEvent(Light target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this LightRemoveEvent object.
	*/
	
	public Light getTarget() {
		return this.target;
	}
	
	/**
	  Returns "LightRemoveEvent".
	*/
	
	public String toString() {
		return "LightRemoveEvent";
	}
}
