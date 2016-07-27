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

package com.almostrealism.photonfield.ui.load;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.JFrame;

import org.almostrealism.tree.ui.ObjectTreeDisplay;

import com.almostrealism.photonfield.ui.DefaultProbabilityDistributionConfigurationPanel;
import com.almostrealism.photonfield.ui.DefaultProbabilityDistributionEditPanel;
import com.almostrealism.photonfield.util.OverlayDistribution;
import com.almostrealism.photonfield.util.ProbabilityDistribution;
import com.almostrealism.photonfield.util.RangeSumDistribution;
import com.almostrealism.photonfield.xml.ProbabilityDistributionDisplay;

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
