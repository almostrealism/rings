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

package com.almostrealism.photon.ui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.text.NumberFormat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.GridLayout;

/**
 *  @author Samuel Tepper
 */
public class SceneDetailPanel extends JPanel
							implements PropertyChangeListener {
	/*
	 * Displays/edits the radial bound of the 
	 * AbsorberHashSet.
	 */
	
	public double Bound, Scale, Duration;
	private JLabel boundLabel, scaleLabel, durLabel;
	private JFormattedTextField boundField, scaleField, durField;
	private NumberFormat numFormat;
	private static String boundString = "Simulation Bound: ";
	private static String scaleString = "Simulation Scale: ";
	private static String durString = "Simulation Duration: ";
					
						
	
	public SceneDetailPanel() {
		
		super(new BorderLayout());
		
		//set up the Fields
		JPanel fields = new JPanel(new GridLayout(0,1));
		
		boundField = new JFormattedTextField(numFormat);
		boundField.setValue(new Double(0.0));
		boundField.setColumns(5);
		boundField.addPropertyChangeListener("bound", this);
		//boundLabel.setHorizontalTextPosition(JLabel.LEADING);
		fields.add(boundField);
		

		scaleField = new JFormattedTextField(numFormat);
		scaleField.setValue(new Double(0.0));
		scaleField.setColumns(5);
		scaleField.addPropertyChangeListener("scale", this);
		//scaleLabel.setHorizontalTextPosition(JLabel.LEADING);
		fields.add(scaleField);
		

		durField = new JFormattedTextField(numFormat);
		durField.setValue(new Double(0.0));
		durField.setColumns(5);
		durField.addPropertyChangeListener("duration", this);
		//durLabel.setHorizontalTextPosition(JLabel.LEADING);
		fields.add(durField);
		
		//set up the Labels
		JPanel labelPanel = new JPanel(new GridLayout(0,1));
		boundLabel = new JLabel(boundString);
		scaleLabel = new JLabel(scaleString);
		durLabel = new JLabel(durString);

		//Add the Labels
		boundLabel.setLabelFor(boundField);
		scaleLabel.setLabelFor(scaleField);
		durLabel.setLabelFor(durField);
		labelPanel.add(boundLabel);
		labelPanel.add(scaleLabel);
		labelPanel.add(durLabel);
		
        //Put the panels in this panel, labels on left,
        //text fields on right.
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(labelPanel, BorderLayout.CENTER);
        add(fields, BorderLayout.LINE_END);
		
		
		//JPanel cameraDisplay = new PinholeCameraAbsorber().getDisplay();
		
		//this.add(cameraDisplay)

	}
	
	public void propertyChange(PropertyChangeEvent e){
		if (e.getSource() == boundField){
			Bound = ((Number)boundField.getValue()).doubleValue();			
		}
		if (e.getSource() == scaleField){
			Scale =  ((Number)boundField.getValue()).doubleValue();
		}
		if (e.getSource() == durField){
			Duration =  ((Number)boundField.getValue()).doubleValue();
		}
	}

}
