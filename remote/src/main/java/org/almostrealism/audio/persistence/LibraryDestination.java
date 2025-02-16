/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.persistence;

import org.almostrealism.io.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

public class LibraryDestination {
	private String prefix;
	private int index;

	public LibraryDestination(String prefix) {
		if (prefix.contains("/")) {
			this.prefix = prefix;
		} else {
			this.prefix = SystemUtils.getLocalDestination(prefix);
		}
	}

	protected String nextFile() {
		return prefix + "_" + index++ + ".bin";
	}

	public Supplier<InputStream> in() {
		return () -> {
			try {
				File f = new File(nextFile());
				if (!f.exists()) return null;

				return new FileInputStream(f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public Supplier<OutputStream> out() {
		return () -> {
			try {
				return new FileOutputStream(nextFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
