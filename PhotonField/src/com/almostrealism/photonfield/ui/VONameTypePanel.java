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
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import java.text.*;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.*;
import java.awt.GridLayout;

public class VONameTypePanel extends JPanel implements PropertyChangeListener, ActionListener {

	public static String name;
	public Object[] typeslist;
	public String[] stringList;
	private JFormattedTextField nameField;
	private MessageFormat msgFormat;
	private JComboBox typeBox;
	private JLabel nameLabel, typeLabel;
	
	public VONameTypePanel() {
		super(new BorderLayout());
		JPanel fields = new JPanel(new GridLayout(0,1));
		
		nameField = new JFormattedTextField(msgFormat);
		nameField.addPropertyChangeListener(this);
		fields.add(nameField);
		
		typeBox = new JComboBox(stringList);
		typeBox.setEditable(true);
        typeBox.addActionListener(this);
	}
	
	public void propertyChange(PropertyChangeEvent e){
		
	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
