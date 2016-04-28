/*
* Copyright (C) 2004  Mike Murray
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License (version 2)
*  as published by the Free Software Foundation.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*/

package net.sf.j3d.ui.themes;


import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import net.sf.j3d.run.Settings;

public class DefaultUITheme extends DefaultMetalTheme {
  private final ColorUIResource primary1 = new ColorUIResource(java.awt.Color.yellow);
  private final ColorUIResource primary2 = new ColorUIResource(java.awt.Color.blue.darker());
  private final ColorUIResource primary3 = new ColorUIResource(java.awt.Color.blue);
  
  private final ColorUIResource secondary1 = new ColorUIResource(69, 69, 69);
  private final ColorUIResource secondary2 = new ColorUIResource(120, 120, 120);
  private final ColorUIResource secondary3 = new ColorUIResource(171, 171, 171);

	public String getName() { return "Default ThreeD App Theme"; }
	
	protected ColorUIResource getPrimary1() { return new ColorUIResource(Settings.themePrimary1); }
	protected ColorUIResource getPrimary2() { return new ColorUIResource(Settings.themePrimary2); }
	protected ColorUIResource getPrimary3() { return new ColorUIResource(Settings.themePrimary3); }
	
	protected ColorUIResource getSecondary1() { return new ColorUIResource(Settings.themeSecondary1); }
	protected ColorUIResource getSecondary2() { return new ColorUIResource(Settings.themeSecondary2); }
	protected ColorUIResource getSecondary3() { return new ColorUIResource(Settings.themeSecondary3); }
}
