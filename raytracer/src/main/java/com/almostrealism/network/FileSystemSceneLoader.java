package com.almostrealism.network;

import org.almostrealism.space.Scene;
import org.almostrealism.space.SceneLoader;

import java.io.FileInputStream;
import java.io.IOException;

public class FileSystemSceneLoader implements SceneLoader {
	@Override
	public Scene apply(String s) {
		try {
			return FileDecoder.decodeScene(new FileInputStream(s), FileDecoder.XMLEncoding,
					false, Exception::printStackTrace);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
