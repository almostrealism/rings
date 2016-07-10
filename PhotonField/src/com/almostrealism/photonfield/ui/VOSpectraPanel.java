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
