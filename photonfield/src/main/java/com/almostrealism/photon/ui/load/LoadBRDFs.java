/*
 * Copyright 2017 Michael Murray
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

package com.almostrealism.photon.ui.load;

import io.almostrealism.tree.ui.ObjectTreeDisplay;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.almostrealism.stats.SphericalProbabilityDistribution;

import com.almostrealism.physics.SpecularAbsorber;
import com.almostrealism.stats.OverlayBRDF;
import com.almostrealism.stats.ReflectiveProbabilityDistribution;
import com.almostrealism.stats.RefractiveProbabilityDistribution;
import com.almostrealism.stats.UniformHemisphericalDistribution;
import com.almostrealism.geometry.Sphere;
import com.almostrealism.photon.ui.AbsorberPreviewPanel;

/**
 * @author  Michael Murray
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
