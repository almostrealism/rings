/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */

/**
 *  @author Samuel Tepper
 */


package net.sf.j3d.physics.pfield.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import net.sf.j3d.physics.pfield.util.*;

public class VOSpectraPanel extends JPanel{

	private JLabel nameLabel;
	public String nameString;
	private ProbabilityDistribution spectrum;
	
	public VOSpectraPanel() {
		super(new FlowLayout());
		nameString = spectrum.getName();
		nameLabel = new JLabel(nameString);
	}

}
