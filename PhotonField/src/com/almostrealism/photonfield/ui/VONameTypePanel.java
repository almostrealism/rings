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

/**
 *  @author Samuel Tepper
 */
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
