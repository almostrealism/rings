package com.almostrealism.photon.network;

import com.almostrealism.network.FileDecoder;
import io.almostrealism.resource.Resource;
import io.flowtree.fs.OutputServer;
import org.almostrealism.space.Scene;
import org.almostrealism.space.SceneLoader;

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
