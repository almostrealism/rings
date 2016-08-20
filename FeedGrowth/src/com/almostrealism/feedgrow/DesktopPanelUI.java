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
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 * @author  Michael Murray
 */
public class DesktopPanelUI extends JPanel {
	private final JPanel panel = new JPanel();
	
	protected JButton btnX;
	protected JToolBar toolBar;

	/**
	 * Create the panel.
	 */
	public DesktopPanelUI() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(panel);
		
		btnX = new JButton("X");
		panel.add(btnX);
		
		toolBar = new JToolBar();
		panel.add(toolBar);
	}

}
