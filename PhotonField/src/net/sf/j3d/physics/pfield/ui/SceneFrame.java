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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

import net.sf.j3d.physics.pfield.raytracer.PinholeCameraAbsorber;
import net.sf.j3d.physics.pfield.xml.Node;

public class SceneFrame{

	/*
	 * The SceneFrame provides data about the simulation.
	 * It is a child of the SceneFrame.
	 */

	public static void CreateAndShowGUI() throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException{
		JFrame frame = new JFrame("Scene Information");
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		double norm[] = {0.0, 0.0, 1.0};
		double orient[] = {0.0, 1.0, 0.0};
		
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
