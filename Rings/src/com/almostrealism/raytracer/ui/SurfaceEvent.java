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
import com.almostrealism.ui.Event;

/** A SurfaceEvent object represents an event that targets a surface. */
public interface SurfaceEvent extends Event {
	/** Returns the target of this SurfaceEvent object. */
	public Surface getTarget();
}
