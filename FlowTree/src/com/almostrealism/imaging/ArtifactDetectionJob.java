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

package com.almostrealism.imaging;

import java.io.File;
import java.io.IOException;

import com.almostrealism.flow.Job;
import com.almostrealism.flow.db.Client;
import com.almostrealism.raytracer.io.FileEncoder;
import com.almostrealism.util.graphics.RGB;

public class ArtifactDetectionJob implements Job {
	private static int ignoreTop = 1800;
	
	private long id;
	private int x, dx;
	private String uri;
	private boolean bot;
	
	public long getTaskId() { return this.id; }
	
	public String getTaskString() { return "ArtifactDetectionTask (" + this.id + ")"; }
	
	public String encode() {
		StringBuffer b = new StringBuffer();
		
		b.append(this.getClass().getName());
		b.append(":id=");
		b.append(this.id);
		b.append(":x=");
		b.append(this.x);
		b.append(":dx=");
		b.append(this.dx);
		b.append(":uri=");
		b.append(this.uri);
		b.append(":b=");
		b.append(this.bot);
		
		return b.toString();
	}
	
	public String toString() { return "ArtifactDetectionJob (" + this.id + ")"; }
	
	public void set(String key, String value) {
		if (key.equals("id")) {
			this.id = Long.parseLong(value);
		} else if (key.equals("x")) {
			this.x = Integer.parseInt(value);
		} else if (key.equals("dx")) {
			this.dx = Integer.parseInt(value);
		} else if (key.equals("uri")) {
			this.uri = value;
		} else if (key.equals("b")) {
			this.bot = Boolean.parseBoolean(value);
		}
	}
	
	public void run() {
		int w = this.dx;
		int h = - ignoreTop;
		
		RGB rgb[][] = null;
		
		try {
			if (this.bot)
				rgb = Client.getCurrentClient().getServer()
						.loadImage(this.uri, x, 0, w, h, false, true);
			else
				rgb = Client.getCurrentClient().getServer()
						.loadImage(this.uri, x, ignoreTop, w, h, false, true);
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace(System.out);
			throw iae;
		}
		
		if (rgb == null) throw new RuntimeException("No image data.");
		
//		if (ArtifactDetector.verbose) System.out.print("Converting image: ");
//		RGB rgb[][] = GraphicsConverter.convertToRGBArray(im);
//		if (ArtifactDetector.verbose) System.out.println("Done");
		
		if (ArtifactDetector.verbose) System.out.println("Initializing detector...");
		ArtifactDetector detector =
			new ArtifactDetector(rgb, rgb.length, rgb[0].length);
		
		// boolean e[][] = detector.scan(0.01, 0.25, 0.55, 0.95, 8, 3);
		// boolean e[][] = detector.scan(0.00, 0.1, 0.3, 1.0, 8, 2, 100);
		boolean e[][] = detector.scan(0.01, 0.08, 0.3, 1.0, 8, 2, 100);
		
		int tot = 0;
		boolean err = false;
		
		i: for (int i = 0; i < e.length; i++) {
			for (int j = 0; j < e[i].length; j++) {
				if (e[i][j]) tot++;
				
				if (tot > this.dx * 0.0) {
					err = true;
					break i;
				}
			}
		}
		
		if (err) {
			try {
				String output = uri.substring(uri.lastIndexOf("/") + 1);
				output = output.substring(0, output.lastIndexOf("."));
				output = output + "-" + x + "-err.jpeg";
				
				System.out.print("Writing " + output + ": ");
				FileEncoder.encodeImageFile(detector.getImage(),
											new File(output),
											FileEncoder.JPEGEncoding);
				System.out.println("Done");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
