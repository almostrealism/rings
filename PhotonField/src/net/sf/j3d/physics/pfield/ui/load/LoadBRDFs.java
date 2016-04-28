/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.ui.load;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.sf.j3d.physics.pfield.SpecularAbsorber;
import net.sf.j3d.physics.pfield.distribution.OverlayBRDF;
import net.sf.j3d.physics.pfield.distribution.ReflectiveProbabilityDistribution;
import net.sf.j3d.physics.pfield.distribution.RefractiveProbabilityDistribution;
import net.sf.j3d.physics.pfield.distribution.SphericalProbabilityDistribution;
import net.sf.j3d.physics.pfield.distribution.UniformHemisphericalDistribution;
import net.sf.j3d.physics.pfield.geometry.Sphere;
import net.sf.j3d.physics.pfield.ui.AbsorberPreviewPanel;
import net.sf.j3d.ui.ObjectTreeDisplay;

/**
 * @author  Mike Murray
 */
public class LoadBRDFs extends TreeObjectLoader {
	public Class getParentType() { return SphericalProbabilityDistribution.class; }
	
	public Class[] loadTypes() {
		return new Class[] {
				ReflectiveProbabilityDistribution.class,
				RefractiveProbabilityDistribution.class,
				UniformHemisphericalDistribution.class
		};
	}
	
	public Hashtable loadOperations() {
		Hashtable t = new Hashtable();
		t.put(TreeObjectLoader.overlay, OverlayBRDF.getOverlayMethod());
		return t;
	}
	
	public static void main(String args[]) {
		final ObjectTreeDisplay display = (ObjectTreeDisplay) new LoadBRDFs().getUI();
		final SpecularAbsorber sa = new SpecularAbsorber();
		sa.setVolume(new Sphere(5.0));
		final AbsorberPreviewPanel preview = new AbsorberPreviewPanel(sa);
		
		JFrame treeFrame = new JFrame("BRDF Tree");
		treeFrame.getContentPane().add(display);
		
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sa.setBRDF((SphericalProbabilityDistribution)display.getRootObject());
					preview.startUpdate();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("BRDF Demo: " + ex);
				}
			}
		});
		
		JFrame previewFrame = new JFrame("Preview");
		previewFrame.setSize(155, 260);
		previewFrame.getContentPane().add(preview, BorderLayout.CENTER);
		previewFrame.getContentPane().add(updateButton, BorderLayout.SOUTH);
		treeFrame.pack();
		previewFrame.setVisible(true);
		treeFrame.setVisible(true);
		treeFrame.setSize(450, 300);
		treeFrame.invalidate();
	}
}
