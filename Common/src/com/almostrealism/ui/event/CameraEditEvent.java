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

import com.almostrealism.raytracer.camera.Camera;

/**
 * A CameraEditEvent object represents the event of editing the camera in the current scene.
 * The integer code of the CameraEditEvent is the sum of all of the codes that apply to the edit.
 * 
 * @author Mike Murray
 */
public class CameraEditEvent extends SceneEditEvent implements CameraEvent {
  /** The code for a location change event. */
  public static final int locationChangeEvent = 1;
  
  /** The code for a viewing direction change event. */
  public static final int viewingDirectionChangeEvent = 1 << 1;
  
  /** The code for an up direction change event. */
  public static final int upDirectionChangeEvent = 1 << 2;
  
  /** The code for a focal length change event. */
  public static final int focalLengthChangeEvent = 1 << 3;
  
  /** The code for a projection dimensions change event. */
  public static final int projectionDimensionsChangeEvent = 1 << 4;
  
  private int code = 0;
  private Camera target;

	/**
	 * Constructs a new CameraEditEvent object with the specified integer code and target.
	 */
	public CameraEditEvent(int code, Camera target) {
		this.code = code;
		this.target = target;
	}
	
	/**
	 * Returns the integer code of this CameraEditEvent object.
	 */
	public int getCode() { return this.code; }
	
	/**
	 * Returns the target of this CameraEditEvent object.
	 */
	public Camera getTarget() { return this.target; }
	
	/**
	 * Returns true if this CameraEditEvent object is a location change event.
	 */
	public boolean isLocationChangeEvent() {
		if ((this.code & CameraEditEvent.locationChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if this CameraEditEvent object is a viewing direction change event.
	 */
	public boolean isViewingDirectionChangeEvent() {
		if ((this.code & CameraEditEvent.viewingDirectionChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if this CameraEditEvent object is an up direction change event.
	 */
	public boolean isUpDirectionChangeEvent() {
		if ((this.code & CameraEditEvent.upDirectionChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if this CameraEditEvent object is a focal length change event.
	 */
	public boolean isFocalLengthChangeEvent() {
		if ((this.code & CameraEditEvent.focalLengthChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if this CameraEditEvent object is a projection dimensions change event.
	 */
	public boolean isProjectionDimensionsChangeEvent() {
		if ((this.code & CameraEditEvent.projectionDimensionsChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns "CameraEditEvent".
	 */
	public String toString() { return "CameraEditEvent"; }
}
