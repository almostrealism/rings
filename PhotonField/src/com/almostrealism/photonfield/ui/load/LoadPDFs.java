/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.ui.load;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.JFrame;

import com.almostrealism.photonfield.ui.DefaultProbabilityDistributionConfigurationPanel;
import com.almostrealism.photonfield.ui.DefaultProbabilityDistributionEditPanel;
import com.almostrealism.photonfield.util.OverlayDistribution;
import com.almostrealism.photonfield.util.ProbabilityDistribution;
import com.almostrealism.photonfield.util.RangeSumDistribution;
import com.almostrealism.photonfield.xml.ProbabilityDistributionDisplay;

import net.sf.j3d.ui.ObjectTreeDisplay;

/**
 * @author  Mike Murray
 */
public class LoadPDFs extends TreeObjectLoader {
	public Class getParentType() { return ProbabilityDistribution.class; }
	
	public Class[] loadTypes() {
		return new Class[] {
				ProbabilityDistribution.class,
		};
	}
	
	public Hashtable loadOperations() {
		Hashtable t = new Hashtable();
		t.put(TreeObjectLoader.overlay, OverlayDistribution.getOverlayMethod());
		return t;
	}
	
	public ObjectTreeDisplay getUI() {
		ObjectTreeDisplay d = (ObjectTreeDisplay) super.getUI();
		d.setTreeCellRenderer(new ProbabilityDistributionDisplay());
		d.addConfigurationDialogType(ProbabilityDistribution.class,
					DefaultProbabilityDistributionConfigurationPanel.class);
		d.addEditPanelType(ProbabilityDistribution.class,
							DefaultProbabilityDistributionEditPanel.class);
		d.addMethodType(RangeSumDistribution.getOverlayMethod());
		d.setBackground(Color.black);
		return d;
	}
	
	public static void main(String args[]) {
		ObjectTreeDisplay display = (ObjectTreeDisplay) new LoadPDFs().getUI();
		JFrame treeFrame = new JFrame("Spectrum Tree");
		treeFrame.getContentPane().add(display);
		treeFrame.setSize(450, 300);
		treeFrame.setVisible(true);
	}
}
