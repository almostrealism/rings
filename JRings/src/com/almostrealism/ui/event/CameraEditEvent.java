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
