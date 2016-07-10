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

package com.almostrealism.raytracer;

import java.io.*;

import javax.swing.*;

import com.almostrealism.io.FileDecoder;
import com.almostrealism.io.FileEncoder;
import com.almostrealism.io.JTextAreaPrintWriter;
import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.engine.*;
import com.almostrealism.raytracer.lighting.*;
import com.almostrealism.ui.displays.*;
import com.almostrealism.ui.panels.*;
import com.almostrealism.util.*;
import com.almostrealism.util.graphics.RGB;

/**
  The RenderFile class provides a method for a user to render a scene stored in a text file.
*/

public class RenderFile {
  public static final String help = "Usage: render.sh OPTIONS image-width image-height super-sample-width super-sample-height input-format input-file [output-format] [output-file]\n" +
					"Arguments:\n" +
					"\timage-width - The width of the output image\n" +
					"\timage-height - The height of the output image\n" +
					"\tsuper-sample-width - The width of the super sample for each pixel\n" +
					"\tsuper-sample-height - The height of the super sample for each pixel\n" +
					"\tinput-format - The format the scene data will be in when read from the input file (xml, raw)\n" +
					"\tinput-file - The file to read in for scene data\n" +
					"\toutput-format - The format the output data will be written in (ppm, pix)\n" +
					"\toutput-file - The file to output to, if one is necesary\n" +
					"Options:\n" +
					"\to - Enables debug output\n" +
					"\tr - Enables ray tracing engine debug output\n" +
					"\ts - Enables surface debug output\n" +
					"\tc - Enables camera engine debug output\n" +
					"\te - Enables event system debug output\n" +
					"\tl - Displays license information\n" +
					"\tn - Uses network resources\n" +
					"\tf - Outputs image to a file\n" +
					"\th - Displays this help\n" +
					"\tz - Use no options\n" +
					"Options should follow the execution command with no spaces between options.\n" +
					"For example,\n" +
					"\trender.sh or\n" +
					"Would run with ray tracing engine debug output enabled.";
  
  private static int imageWidth, imageHeight, superSampleHeight, superSampleWidth;
  private static String inputFormat, inputFile, outputFormat, outputFile;
  private static boolean outputImage;

	public static void main(String args[]) {
		if (args.length < 7)
			System.out.println("ERROR: Not enough arguments");
		
		i: for(int i = 0; i < args[0].length(); i++) {
			String option = args[0].substring(i, i + 1);
			
			if (option.equals("o") == true) {
				Settings.produceOutput = true;
			} else if (option.equals("r") == true) {
				Settings.produceRayTracingEngineOutput = true;
				Settings.rayEngineOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			} else if (option.equals("s") == true) {
				Settings.produceSurfaceOutput = true;
				Settings.surfaceOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			} else if (option.equals("c") == true) {
				Settings.produceCameraOutput = true;
				Settings.cameraOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			} else if (option.equals("e") == true) {
				Settings.produceEventHandlerOutput = true;
				Settings.eventOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			} else if (option.equals("l") == true) {
				System.out.println("Copyright 2016 Michael Murray");
				System.exit(0);
			} else if (option.equals("n") == true) {
				Settings.useRemoteResources = true;
			} else if (option.equals("f") == true) {
				RenderFile.outputImage = true;
				RenderFile.outputFormat = args[7];
				RenderFile.outputFile = args[8];
			} else if (option.equals("h") == true) {
				System.out.println(RenderFile.help);
				System.exit(0);
			} else if (option.equals("z") == true) {
				break i;
			} else {
				System.out.println("Unknown option: " + option);
			}
		}
		
		RenderFile.imageWidth = Integer.parseInt(args[1]);
		RenderFile.imageHeight = Integer.parseInt(args[2]);
		RenderFile.superSampleWidth = Integer.parseInt(args[3]);
		RenderFile.superSampleHeight = Integer.parseInt(args[4]);
		
		RenderFile.inputFormat = args[5];
		RenderFile.inputFile = args[6];
		
		System.out.println("threeD.run.RenderFile (Version " + Settings.version + ")\n");
		System.out.println("Copyright 2016 Michael Murray");
		
		if (Settings.produceOutput == true) {
			DebugOutputPanel outputPanel = new DebugOutputPanel();
			outputPanel.showPanel();
		}
		
		System.out.println("Loading scene...");
		
		Scene scene = null;
		
		try {
			if (RenderFile.inputFormat.equals("xml") == true)
				scene = FileDecoder.decodeSceneFile(new File(RenderFile.inputFile), FileDecoder.XMLEncoding, false, null);
			else if (RenderFile.inputFormat.equals("raw") == true)
				scene = FileDecoder.decodeSceneFile(new File(RenderFile.inputFile), FileDecoder.RAWEncoding, false, null);
			else
				System.out.println("Unknown input format: " + RenderFile.inputFormat);
		} catch (FileNotFoundException fnf) {
			System.out.println("ERROR: Input file not found");
		} catch (IOException ioe) {
			System.out.println("IO ERROR");
		}
		
		if (scene == null) {
			System.out.println("ERROR: Unable to load scene");
			System.exit(1);
		}
		
		if (RenderFile.inputFormat.equals("raw") == true) {
			scene.addLight(new DirectionalAmbientLight(0.7, new RGB(1.0, 1.0, 1.0), new Vector(0.0, 1.0, -1.0)));
			scene.addLight(new PointLight(new Vector(4.0, 4.0, 4.0), 0.35, new RGB(1.0, 1.0, 1.0)));
			scene.addLight(new PointLight(new Vector(-4.0, 4.0, 4.0), 0.35, new RGB(1.0, 1.0, 1.0)));
			scene.addLight(new PointLight(new Vector(4.0, -4.0, 4.0), 0.35, new RGB(1.0, 1.0, 1.0)));
			scene.addLight(new PointLight(new Vector(-4.0, -4.0, 4.0), 0.35, new RGB(1.0, 1.0, 1.0)));
			
			scene.setCamera(new PinholeCamera(new Vector(0.0, 0.0, 8.0), new Vector(0.0, 0.0, -1.0), new Vector(0.0, 1.0, 0.0), 3.0, 6.0, 6.0));
		}
		
		int imageEncoding = -1;
		
		if (RenderFile.outputFormat.equals("ppm") == true) {
			imageEncoding = FileEncoder.PPMEncoding;
		} else if (RenderFile.inputFormat.equals("pix") == true) {
			imageEncoding = FileEncoder.PIXEncoding;
		} else {
			System.out.println("Unknown output format: " + RenderFile.outputFormat);
			System.exit(2);
		}
		
		
		JFrame frame = new JFrame("Progress");
		frame.setSize(150, 100);
		
		ProgressDisplay monitor = new ProgressDisplay(1, RenderFile.imageWidth * RenderFile.imageHeight);
		
		frame.getContentPane().add(monitor);
		frame.setVisible(true);
		
		System.out.println("Ray tracing...");
		
		RGB image[][] = RayTracingEngine.render(scene, RenderFile.imageWidth, RenderFile.imageHeight, RenderFile.superSampleWidth, RenderFile.superSampleHeight, monitor);
		
		try {
			FileEncoder.encodeImageFile(image, new File(RenderFile.outputFile), imageEncoding);
		} catch (FileNotFoundException fnf) {
			System.out.println("ERROR: Output file not found");
		} catch (IOException ioe) {
			System.out.println("IO ERROR");
		}
	}
}
