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

package com.almostrealism;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.almostrealism.ui.Event;
import org.almostrealism.ui.EventGenerator;
import org.almostrealism.ui.EventHandler;
import org.almostrealism.ui.EventListener;

import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.surfaceUI.RenderPanel;
import com.almostrealism.raytracer.surfaceUI.SplashScreen;
import com.almostrealism.raytracer.ui.DebugOutputPanel;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.raytracer.ui.menus.DefaultToolBar;
import com.almostrealism.ui.JTextAreaPrintWriter;

/**
 * The RayTracer class uses standard ui components from the threeD.ui package to provide a user interface
 * for the ray tracing engine.
 * 
 * @author Mike Murray
 */
public class RayTracer implements EventListener, EventGenerator {
  public static final String help = "Usage: rings [OPTIONS] [SUB-OPTIONS]\n" +
					"Options:\n" +
					"\tp - Uses specified properties file\n" +
					"\to - Enables debug output\n" +
					"\tr - Enables ray tracing engine debug output\n" +
					"\ts - Enables surface debug output\n" +
					"\tc - Enables camera engine debug output\n" +
					"\te - Enables event system debug output\n" +
					"\tl - Displays license information\n" +
					"\tt - Activates themes/plafs\n" +
					"\tn - Uses network resources\n" +
					"\th - Displays this help\n" +
					"Options should follow the execution command with no spaces between options.\n" +
					"For example,\n" +
					"\trings or\n" +
					"Would run rings with debug output enabled.\n" +
					"Also, options with sub-options should have their sub-options specified in the order the option was specified.\n" +
					"For example,\n" +
					"\trings t default\n" +
					"Would run rings using the default theme.";
  
  private EventHandler handler;
  
  private Scene scene;
  
  private JFrame frame;
  
  private RenderPanel renderPanel;
  private DefaultToolBar menuBar;

	/**
	 * Starts the ray tracing interface.
	 */
	public static void main(String args[]) {
		String theme = "default";
		
		if (args.length > 0) {
			int subOptions = 0;
			
			for(int i = 0; i < args[0].length(); i++) {
				String option = args[0].substring(i, i + 1);
				
				if (option.equals("p") == true) {
					try {
						System.out.println("Loading properties file...");
						
						Properties p = new Properties();
						p.load(new FileInputStream(new File(args[++subOptions])));
						
						Settings.setProperties(p);
					} catch (FileNotFoundException fnf) {
						System.out.println("Properties file not found.");
					} catch (IOException ioe) {
						System.out.println("IO error while loading properties file.");
					}
				} else if (option.equals("o") == true) {
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
				} else if (option.equals("t") == true) {
					theme = args[++subOptions];
				} else if (option.equals("n") == true) {
					Settings.useRemoteResources = true;
				} else if (option.equals("h") == true) {
					System.out.println(RayTracer.help);
					System.exit(0);
				} else {
					System.out.println("Unknown option: " + option);
				}
			}
		}
		
		System.out.println("Rings (Version " + Settings.version + ")\n");
		System.out.println("Copyright 2016 Michael Murray");
		
		try {
			if (theme.equals("default")) {
//				System.out.println("Loading Default Theme...");
//				MetalTheme defaultTheme = new DefaultUITheme();
//				
//				System.out.println("Loading Kunststoff Look And Feel...");
//				com.incors.plaf.kunststoff.KunststoffLookAndFeel plaf = new com.incors.plaf.kunststoff.KunststoffLookAndFeel();
//				com.incors.plaf.kunststoff.KunststoffLookAndFeel.setCurrentTheme(defaultTheme);
//				
//				UIManager.setLookAndFeel(plaf);
			} else if (theme.equals("kunststoff")) {
				System.out.println("Loading Kunststoff Look And Feel...");
				UIManager.setLookAndFeel("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
			} else if (theme.equals("3d")) {
				System.out.println("Loading 3D Look And Feel...");
				UIManager.setLookAndFeel("swing.addon.plaf.threeD.ThreeDLookAndFeel");
			} else if (theme.equals("fh")) {
				System.out.println("Loading fh Look And Feel...");
				UIManager.setLookAndFeel("com.shfarr.ui.plaf.fh.FhLookAndFeel");
			} else if (theme.equals("none")) {
			} else {
				UIManager.setLookAndFeel(theme);
			}
		} catch (ClassNotFoundException ce) {
			System.out.println("Theme/plaf not found");
		} catch (Exception e) {
			System.out.println("Theme/plaf Error");
		}
		
		SplashScreen splash = null;
		
		if (Settings.useRemoteResources == true) {
			try {
				splash = new SplashScreen(java.awt.Toolkit.getDefaultToolkit().createImage(new URL(Settings.remoteSplashImage)));
			} catch (MalformedURLException me) {
				System.out.println("Network resource URL was malformed");
			}
		} else {
			// if ((new File(Settings.localSplashImage)).canRead()) {
				URL data = (RayTracer.class).getClassLoader().getResource(Settings.localSplashImage);
				if (data != null)
					splash = new SplashScreen((new ImageIcon(data)).getImage());
			// } else {
			//	splash = new SplashScreen(null);
			// }
		}
		
		if (splash != null) splash.setVisible(true);
		
		try {
			Thread.sleep(2000);
		} catch(InterruptedException ie) {
			System.out.println("Thread Sleep Was Interrupted");
		}
		
		Settings.init();
		
		if (Settings.produceOutput == true) {
			DebugOutputPanel outputPanel = new DebugOutputPanel();
			outputPanel.showPanel();
		}
		
		RayTracer raytracer = new RayTracer(new Scene());
		
		EventHandler newHandler = new EventHandler();
		
		raytracer.setEventHandler(newHandler);
		newHandler.addListener(raytracer);
		
		try {
			Thread.sleep(1000);
		} catch(InterruptedException ie) {
			System.out.println("Thread Sleep Was Interrupted");
		}
		
		if (splash != null) splash.setVisible(false);
	}
	
	/**
	 * Constructs a new RayTracer object using the specified Scene object.
	 */
	public RayTracer(Scene scene) {
		this.scene = scene;
		
		this.frame = new JFrame("Rings");
		
		this.renderPanel = new RenderPanel(this.scene);
		this.menuBar = new DefaultToolBar(this.scene, this.renderPanel);
		
		this.frame.getContentPane().add(this.menuBar, java.awt.BorderLayout.NORTH);
		this.frame.getContentPane().add(this.renderPanel, java.awt.BorderLayout.CENTER);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				System.exit(0);
			}
		});
		
		this.frame.setSize(400, 400);
		this.frame.setVisible(true);
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
		}
	}
	
	/**
	 * Sets the EventHandler object used by the ray tracing interface. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.renderPanel.setEventHandler(this.handler);
		this.menuBar.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.renderPanel);
			this.handler.addListener(this.menuBar);
		}
	}
	
	/**
	 * Returns the EventHandler object used by the ray tracing interface.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
