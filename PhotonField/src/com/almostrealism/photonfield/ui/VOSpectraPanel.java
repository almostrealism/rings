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


package com.almostrealism.photonfield.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;

import com.almostrealism.photonfield.util.*;

import java.awt.FlowLayout;

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
