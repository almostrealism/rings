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

package com.almostrealism.gl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import com.almostrealism.renderable.GLDriver;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import org.almostrealism.texture.ImageSource;

public class TextureManager {
	private HashMap<ImageSource, Integer> textures;
	
	public TextureManager() {
		this.textures = new HashMap<ImageSource, Integer>();
	}
	
	public void addTexture(GLDriver gl, ImageSource s) {
		if (textures.containsKey(s)) return;

		int tex = put(gl, s);

		int pixels[] = s.getPixels();
		int width = s.getWidth();
		int height = s.getHeight();
		
		byte data[];

		if (!s.isAlpha()) {
			data = new byte[pixels.length * 3];

			for (int y = height - 1, pointer = 0; y >= 0; y--) {
				for (int x = 0; x < width; x++, pointer += 3) {
					data[pointer + 0] = (byte)((pixels[y * width + x] >> 16) & 0xFF);
					data[pointer + 1] = (byte)((pixels[y * width + x] >>  8) & 0xFF);
					data[pointer + 2] = (byte) (pixels[y * width + x]        & 0xFF);
				}
			}
		} else {
			data = new byte[pixels.length * 4];

			for (int y = height - 1, pointer = 0; y >= 0; y--) {
				for (int x = 0; x < width; x++,pointer += 4) {
					data[pointer + 3] = (byte)((pixels[y * width + x] >> 24) & 0xFF);
					data[pointer + 0] = (byte)((pixels[y * width + x] >> 16) & 0xFF);
					data[pointer + 1] = (byte)((pixels[y * width + x] >>  8) & 0xFF);
					data[pointer + 2] = (byte) (pixels[y * width + x]        & 0xFF);
				}
			}
		}
		
		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);
		
		if (s.isAlpha()) {
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, 128, 128,
							0, GL.GL_RGBA,GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data));
		} else {
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, 128, 128,
							0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data));
		}
		
		gl.glTexParameter(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameter(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	}

	private int put(GLDriver gl, ImageSource s) {
		IntBuffer buf = IntBuffer.allocate(1);
		gl.glGenTextures(1, buf);
		
		int tex = buf.get(0);
		textures.put(s, tex);
		return tex;
	}
	
	public void pushTexture(GLDriver gl, ImageSource s) {
		addTexture(gl, s);
		
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures.get(s));
	}
	
	public void popTexture(GLDriver gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
	}
}
