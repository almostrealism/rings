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

/*
* Copyright (C) 2004  Mike Murray
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

package com.almostrealism.ui.menus;


import javax.swing.*;

import com.almostrealism.ui.dialogs.*;
import com.almostrealism.ui.panels.*;

/**
  A WindowsMenu object extends JMenu and provides menu items for opening varius windows.
*/

public class WindowsMenu extends JMenu {
  private SurfaceInfoPanel surfaceInfoPanel;
  
  private EditCameraDialog editCameraDialog;
  private EditLightDialog editLightDialog;
  
  private JMenuItem surfaceInfoPanelItem;
  
  private JMenuItem editCameraDialogItem;
  private JMenuItem editLightDialogItem;

	public WindowsMenu(SurfaceInfoPanel surfaceInfoPnl, EditCameraDialog editCameraDlg, EditLightDialog editLightDlg) {
		this.surfaceInfoPanel = surfaceInfoPnl;
		
		this.editCameraDialog = editCameraDlg;
		this.editLightDialog = editLightDlg;
		
		this.surfaceInfoPanelItem = new JMenuItem("Surface Info Panel");
		
		this.editCameraDialogItem = new JMenuItem("Edit Camera Dialog");
		this.editLightDialogItem = new JMenuItem("Edit Light Dialog");
		
		this.add(this.surfaceInfoPanelItem);
		this.addSeparator();
		this.add(this.editCameraDialogItem);
		this.add(this.editLightDialogItem);
		
		this.surfaceInfoPanelItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				surfaceInfoPanel.showPanel();
			}
		});
		
		this.editCameraDialogItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				editCameraDialog.showDialog();
			}
		});
		
		this.editLightDialogItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				editLightDialog.showDialog();
			}
		});
	}
}
