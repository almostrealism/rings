/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.photon.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

import com.almostrealism.absorption.PinholeCameraAbsorber;
import com.almostrealism.photon.xml.Node;
import org.almostrealism.algebra.Vector;

/**
 *  @author Samuel Tepper
 */
public class SceneFrame {

	/*
	 * The SceneFrame provides data about the simulation.
	 * It is a child of the SceneFrame.
	 */

	public static void CreateAndShowGUI() throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException{
		JFrame frame = new JFrame("Scene Information");
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Vector norm = new Vector(0.0, 0.0, 1.0);
		Vector orient = new Vector(0.0, 1.0, 0.0);
		
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(new SceneDetailPanel());
		panel.add(new Node(new PinholeCameraAbsorber(20, 1, norm, orient)).getDisplay().getContainer());
		frame.getContentPane().add(new SceneMenuBar(), BorderLayout.NORTH);
		frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) throws IllegalArgumentException, IntrospectionException,
										IllegalAccessException, InvocationTargetException {
		CreateAndShowGUI();
	}
}
