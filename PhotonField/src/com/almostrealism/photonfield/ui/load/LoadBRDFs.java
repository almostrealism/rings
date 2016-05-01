/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.ui.load;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.almostrealism.photonfield.SpecularAbsorber;
import com.almostrealism.photonfield.distribution.OverlayBRDF;
import com.almostrealism.photonfield.distribution.ReflectiveProbabilityDistribution;
import com.almostrealism.photonfield.distribution.RefractiveProbabilityDistribution;
import com.almostrealism.photonfield.distribution.SphericalProbabilityDistribution;
import com.almostrealism.photonfield.distribution.UniformHemisphericalDistribution;
import com.almostrealism.photonfield.geometry.Sphere;
import com.almostrealism.photonfield.ui.AbsorberPreviewPanel;

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
