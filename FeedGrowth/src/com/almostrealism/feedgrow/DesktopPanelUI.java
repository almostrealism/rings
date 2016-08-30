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

package com.almostrealism.feedgrow;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * @author  Michael Murray
 */
public class DesktopPanelUI extends JPanel {
	private final JPanel panel = new JPanel();
	protected JPanel renderPanel;
	protected JToolBar toolBar;

	/**
	 * Create the panel.
	 */
	public DesktopPanelUI() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		renderPanel = new JPanel();
		panel.add(renderPanel, BorderLayout.CENTER);
		renderPanel.setLayout(new BorderLayout(0, 0));
		
		toolBar = new JToolBar();
		toolBar.setOpaque(true);
		toolBar.setBackground(Color.black);
		toolBar.setForeground(Color.black);
		toolBar.setBorderPainted(false);
		panel.add(toolBar, BorderLayout.SOUTH);
		panel.setOpaque(true);
		panel.setBackground(Color.black);
	}

}
