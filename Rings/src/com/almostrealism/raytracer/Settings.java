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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Properties;
import java.util.Random;

import javax.swing.Icon;

import com.almostrealism.io.PrintWriter;
import com.almostrealism.ui.Defaults;

/**
 * The Settings class provides access to settings that change the way the RayTracer and its components run.
 * This class also includes static fields providing information about this release of the RayTracer.
 * 
 * @author Mike Murray
 */
public abstract class Settings extends Defaults {
  /** String containing the version number of this software. */
  public static final String version = "0.4";
  
  /** String containing "Written by Mike Murray". */
  public static final String writtenByInfo = "Written by Mike Murray";
  
  /** String containing "ashesfall@users.sf.net". */
  private static final String adminEmail = "ashesfall@almostrealism.com";
  
  /** String containing "http://j3d.sf.net/". */
  public static final String websiteUrl = "http://almostrealism.com/";
  
  
  /** String used to indicate this warning is given at random. */
  public static String randomWarningSymbol = "[NOTE] ";
  
  /** Threshold for random warnings. */
  public static double randomWarningThreshold = 0.00005;
  
  /** Integer values for the screen width and height. */
  public static int screenWidth, screenHeight;
  
  /** Setting to true enables remote resource use. */
  public static boolean useRemoteResources = false;
  
  /** String containing the local directory where resources can be found. */
  public static String resourcePath = "";
  
  /** String containing the local location of the splash screen image. */
  public static String localSplashImage = Settings.resourcePath + "splash.jpg";
  
  /** String containing the remote location of the splash screen image. */
  public static String remoteSplashImage = "http://j3d.sourceforge.net/jws/" + "splash.jpg";
  
  
  /** Setting to true enables debug output. */
  public static boolean produceOutput = false;
  
  /** Setting to true enables ray tracing engine debug output. */
  public static boolean produceRayTracingEngineOutput = false;
  
  /** A JTextAreaPrintWriter object that should be used for debug output from the ray tracing engine. */
  public static PrintWriter rayEngineOut = null;
  
  /** Setting to true enables shader debug output. */
  public static boolean produceShaderOutput = false;
  
  public static PrintWriter shaderOut = null;
  
  /** Setting to true enables surface debug output. */
  public static boolean produceSurfaceOutput = false;
  
  /** A JTextAreaPrintWriter object that should be used for debug output from surfaces. */
  public static PrintWriter surfaceOut = null;
  
  /** Setting to true enables camera debug output. */
  public static boolean produceCameraOutput = false;
  
  /** A JTextAreaPrintWriter object that should be used for debug output from the camera. */
  public static PrintWriter cameraOut = null;
  
  /** Setting to true enables event system debug output. */
  public static boolean produceEventHandlerOutput = false;
  
  /** A JTextAreaPrintWriter object that should be used for debug output from the event system. */
  public static PrintWriter eventOut = null;
  
  /** Path to file to load scene icon from. */
  public static final String sceneIconFile = Settings.resourcePath + "scene.jpg";
  
  /** Icon to use for top level of surface tree. */
  public static Icon sceneIcon;
  
  /** A general purpose instance of Random to be used whenever necessary. */
  public static Random random = new Random();
  
  /** AWT Color representing the primary 1 color for the default theme. */
  public static Color themePrimary1 = (new Color(0, 108, 175)).darker();
  
  /** AWT Color representing the primary 2 color for the default theme. */
  public static Color themePrimary2 = new Color(0, 108, 175);
  
  /** AWT Color representing the primary 3 color for the default theme. */
  public static Color themePrimary3 =  Settings.themePrimary2.brighter();
  
  /** AWT Color representing the secondary 1 color for the default theme. */
  public static Color themeSecondary1 = new Color(102, 103, 104);
  
  /** AWT Color representing the secondary 2 color for the default theme. */
  public static Color themeSecondary2 = new Color(170, 174, 177);
  
  /** AWT Color representing the secondary 3 color for the default theme. */
  public static Color themeSecondary3 = new Color(209, 212, 214);

  
  	
	public static void init() {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Settings.screenWidth = screenDim.width;
		Settings.screenHeight = screenDim.height;
	}
	
	/**
	 * Sets the values of the static variables of the Settings class to those
	 * described by the specified Properties object.
	 */
	public static void setProperties(Properties p) {
		Settings.useRemoteResources = (Boolean.valueOf(p.getProperty("resources.use_remote", String.valueOf(Settings.useRemoteResources)))).booleanValue();
		Settings.resourcePath = p.getProperty("resources.local.path", Settings.resourcePath);
		Settings.localSplashImage = Settings.resourcePath + "splash.jpg";
		
		Settings.produceOutput = (Boolean.valueOf(p.getProperty("output.produce", String.valueOf(Settings.produceOutput)))).booleanValue();
		Settings.produceRayTracingEngineOutput = (Boolean.valueOf(p.getProperty("output.ray_tracing_engine.produce", String.valueOf(Settings.produceRayTracingEngineOutput)))).booleanValue();
		Settings.produceShaderOutput = (Boolean.valueOf(p.getProperty("output.shader.produce", String.valueOf(Settings.produceShaderOutput)))).booleanValue();
		Settings.produceSurfaceOutput = (Boolean.valueOf(p.getProperty("output.surface.produce", String.valueOf(Settings.produceSurfaceOutput)))).booleanValue();
		Settings.produceCameraOutput = (Boolean.valueOf(p.getProperty("output.camera.produce", String.valueOf(Settings.produceCameraOutput)))).booleanValue();
		Settings.produceEventHandlerOutput = (Boolean.valueOf(p.getProperty("output.event_handler.produce", String.valueOf(Settings.produceEventHandlerOutput)))).booleanValue();
		
		Settings.themePrimary1 = Color.decode(p.getProperty("colors.theme.primary1", String.valueOf(Settings.themePrimary1.getRGB())));
		Settings.themePrimary2 = Color.decode(p.getProperty("colors.theme.primary2", String.valueOf(Settings.themePrimary2.getRGB())));
		Settings.themePrimary3 = Color.decode(p.getProperty("colors.theme.primary3", String.valueOf(Settings.themePrimary3.getRGB())));
		Settings.themeSecondary1 = Color.decode(p.getProperty("colors.theme.secondary1", String.valueOf(Settings.themeSecondary1.getRGB())));
		Settings.themeSecondary2 = Color.decode(p.getProperty("colors.theme.secondary2", String.valueOf(Settings.themeSecondary2.getRGB())));
		Settings.themeSecondary3 = Color.decode(p.getProperty("colors.theme.secondary3", String.valueOf(Settings.themeSecondary3.getRGB())));
	}
	
	/**
	 * Returns a Properties object that stores the current values of the static variables
	 * of the Settings class.
	 */
	public static Properties getProperties() {
		Properties p = new Properties();
		
		p.setProperty("resources.use_remote", String.valueOf(Settings.useRemoteResources));
		p.setProperty("resources.local.path", String.valueOf(Settings.resourcePath));
		
		p.setProperty("output.produce", String.valueOf(Settings.produceOutput));
		p.setProperty("output.ray_tracing_engine.produce", String.valueOf(Settings.produceRayTracingEngineOutput));
		p.setProperty("output.shader.produce", String.valueOf(Settings.produceShaderOutput));
		p.setProperty("output.surface.produce", String.valueOf(Settings.produceSurfaceOutput));
		p.setProperty("output.camera.produce", String.valueOf(Settings.produceCameraOutput));
		p.setProperty("output.event_handler.produce", String.valueOf(Settings.produceEventHandlerOutput));
		
		p.setProperty("colors.theme.primary1", String.valueOf(Settings.themePrimary1.getRGB()));
		p.setProperty("colors.theme.primary2", String.valueOf(Settings.themePrimary2.getRGB()));
		p.setProperty("colors.theme.primary3", String.valueOf(Settings.themePrimary3.getRGB()));
		p.setProperty("colors.theme.secondary1", String.valueOf(Settings.themeSecondary1.getRGB()));
		p.setProperty("colors.theme.secondary2", String.valueOf(Settings.themeSecondary2.getRGB()));
		p.setProperty("colors.theme.secondary3", String.valueOf(Settings.themeSecondary3.getRGB()));
		
		return p;
	}
}
