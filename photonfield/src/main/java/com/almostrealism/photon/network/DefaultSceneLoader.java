package com.almostrealism.photon.network;

import com.almostrealism.io.SceneLoader;
import io.flowtree.fs.OutputServer;
import io.almostrealism.code.Resource;
import org.almostrealism.space.FileDecoder;
import org.almostrealism.space.Scene;

import java.io.IOException;

/**
 * A default implementation of {@link SceneLoader} which uses the
 * {@link io.flowtree.node.Server#loadResource(String)} method to
 * obtain an {@link java.io.InputStream} for {@link FileDecoder}.
 *
 * @author  Michael Murray
 */
public class DefaultSceneLoader implements SceneLoader {
	@Override
	public Scene apply(String s) {
		try {
			Resource r = OutputServer.getCurrentServer().getNodeServer().loadResource(s);
			return FileDecoder.decodeScene(r.getInputStream(), FileDecoder.XMLEncoding,
										false, Exception::printStackTrace);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
