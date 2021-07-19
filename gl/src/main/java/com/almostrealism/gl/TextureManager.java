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

import java.util.HashMap;

import com.jogamp.opengl.GL;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.almostrealism.texture.ImageSource;

@Deprecated
public class TextureManager {
	private HashMap<ImageSource, Texture> textures;
	
	public TextureManager() {
		this.textures = new HashMap<>();
	}
	
	public void addTexture(GLDriver gl, ImageSource s) {
		if (textures.containsKey(s)) return;

		Texture tex = put(gl, s);

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
		
		gl.bindTexture(tex);
		
		if (s.isAlpha()) {
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, 128, 128,
							0, GL.GL_RGBA,GL.GL_UNSIGNED_BYTE, data);
		} else {
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, 128, 128,
							0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data);
		}
		
		gl.glTexParameter(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameter(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	}

	private Texture put(GLDriver gl, ImageSource s) {
//		textures.put(s, TextureIO.newTexture(new TextureData())); TODO
		return null;
	}
	
	public void pushTexture(GLDriver gl, ImageSource s) {
		addTexture(gl, s);

        gl.bindTexture(textures.get(s));
		gl.enableTexture(textures.get(s));
	}
	
	public void popTexture(GLDriver gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
	}
}
