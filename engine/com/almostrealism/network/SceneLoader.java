package com.almostrealism.network;

import org.almostrealism.space.Scene;

import java.util.function.Function;

/**
 * A strategy for loading {@link Scene}.
 *
 * @author  Michael Murray
 */
public interface SceneLoader extends Function<String, Scene> {
}
