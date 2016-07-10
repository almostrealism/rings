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
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.util.buffers;

import java.io.IOException;

import com.almostrealism.photonfield.network.PhotonFieldSceneLoader;
import com.almostrealism.util.graphics.RGB;

public interface ColorBuffer {
	public void addColor(double u, double v, boolean front, RGB c);
	public RGB getColorAt(double u, double v, boolean front);
	public void setScale(double m);
	public double getScale();
	public void clear();
	
	public void store(PhotonFieldSceneLoader loader, String name) throws IOException;
	public void load(PhotonFieldSceneLoader loader, String name) throws IOException;
}