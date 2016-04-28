/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */



package net.sf.j3d.physics.pfield.ui;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.FlowLayout;

import net.sf.j3d.physics.pfield.distribution.OverlayBRDF;


/**
 *  @author Samuel Tepper
 */

public class VOBRDFPanel extends JPanel{

	private JLabel nameLabel;
	private static String nameString;
	private OverlayBRDF currentBRDF;
	
	public VOBRDFPanel(){
		super(new FlowLayout());
		nameString = currentBRDF.getName();
		nameLabel = new JLabel(nameString);
		add(nameLabel);
		}
	
	
}
