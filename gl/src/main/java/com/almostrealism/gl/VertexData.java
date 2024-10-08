/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.gl;

import java.nio.ByteBuffer;

/**
 * Represents information for accessing vertex data.
 * 
 * @author jezek2
 */
public class VertexData {
	public ByteBuffer vertexbase;
	public int numverts;
	public ScalarType type;
	public int stride;
	public ByteBuffer indexbase;
	public int indexstride;
	public int numfaces;
	public ScalarType indicestype;

	/** Unreferences data buffers to avoid memory leaks. */
	public void unref() {
		vertexbase = null;
		indexbase = null;
	}
}
