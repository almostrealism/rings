/*
* Copyright (C) 2004-05  Mike Murray
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License (version 2)
*  as published by the Free Software Foundation.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*/

package com.almostrealism.ui.panels;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;


/**
 * An ExtendedCellRenderer object can be used to render values in a table
 * including instances of RGB and Class.
 */
public class ExtendedCellRenderer extends DefaultTableCellRenderer {
  private Border selectedBorder, unselectedBorder;

	/**
	 * Constructs a new ExtendedCellRenderer object.
	 */
	public ExtendedCellRenderer() {}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus,
							int row, int column) {
		if (value instanceof RGB) {
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setBackground(GraphicsConverter.convertToAWTColor((RGB)value));
			label.setToolTipText(value.toString());
			
			if (isSelected == true) {
				this.selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				label.setBorder(this.selectedBorder);
			} else {
				this.unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				label.setBorder(this.unselectedBorder);
			}
			
			return label;
		} else if (value instanceof Class) {
			String name = ((Class)value).getName();
			name = name.substring(name.lastIndexOf('.') + 1);
			
			if (name.equals("Editable$Selection")) name = "Selection";
			
			JTextArea label = new JTextArea();
			label.setEditable(false);
			label.setLineWrap(true);
			label.setWrapStyleWord(true);
			label.setText(name);
			
			if (isSelected == true) {
				this.selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				label.setBorder(this.selectedBorder);
			} else {
				this.unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				label.setBorder(this.unselectedBorder);
			}
			
			return label;
		} else if (value instanceof String) {
			JTextArea textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setText((String)value);
			
			if (isSelected == true) {
				textArea.setBackground(table.getSelectionBackground());
			} else {
				textArea.setBackground(table.getBackground());
			}
			
			return textArea;
		} else {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}
