/*
 * Copyright 2024 Michael Murray
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

package com.almostrealism.audio.test;

import com.almostrealism.audio.AudioLibraryPersistence;
import org.almostrealism.audio.AudioLibrary;
import org.junit.Test;

import com.almostrealism.audio.api.Audio.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioLibraryTest {
	public static String LIBRARY = "Library";

	static {
		String env = System.getenv("AR_RINGS_LIBRARY");
		if (env != null) LIBRARY = env;

		String arg = System.getProperty("AR_RINGS_LIBRARY");
		if (arg != null) LIBRARY = arg;
	}

	@Test
	public void libraryRefresh() throws IOException {
		AudioLibrary library = AudioLibrary.load(new File(LIBRARY));
		library.refresh();

		AudioLibraryPersistence.saveLibrary(library, "library");
	}
}

