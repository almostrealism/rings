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

// TODO Add pop-up menu
// TODO Add support for moving and renaming surfaces
// TODO Make showPanel method bring window to front if it is open already.

package com.almostrealism.raytracer.ui;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.raytracer.shaders.ShaderFactory;
import com.almostrealism.raytracer.surfaceUI.AbstractSurfaceUI;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;
import com.almostrealism.texture.TextureFactory;
import com.almostrealism.ui.Event;
import com.almostrealism.ui.EventGenerator;
import com.almostrealism.ui.EventHandler;
import com.almostrealism.ui.EventListener;
import com.almostrealism.ui.panels.ExpandedEditPanel;
import com.almostrealism.ui.panels.ShaderEditPanel;

// TODO  Add duplicate surface function.

/**
 * A SurfaceInfoPanel object allows access to a list of SurfaceUI objects contained
 * in the specified Scene object.
 */
public class SurfaceInfoPanel extends JPanel implements EventListener, EventGenerator {
	protected static class SurfaceTreeCellRenderer extends DefaultTreeCellRenderer {
		public SurfaceTreeCellRenderer() {
			Settings.sceneIcon = new ImageIcon(
					Toolkit.getDefaultToolkit().createImage(Settings.sceneIconFile));
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
														boolean leaf, int row, boolean focus) {
			if (value instanceof SceneTreeNode) {
				super.setOpenIcon(Settings.sceneIcon);
				super.setClosedIcon(Settings.sceneIcon);
			} else if (value instanceof SurfaceTreeNode) {
				SurfaceUI s = ((SurfaceTreeNode)value).getSurface();
				super.setOpenIcon(s.getIcon());
				super.setClosedIcon(s.getIcon());
				super.setLeafIcon(s.getIcon());
			}
			
			return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focus);
		}
	}
	
  private static final String editOptions[] = {"Settings", "Textures", "Shaders", "Transform"};
  
  private Scene scene;
  
  private EventHandler handler;
  
  private boolean open;
  
  private EditSurfacePanel settingsPanel;
  private ExpandedEditPanel texturesPanel;
  private ExpandedEditPanel shadersPanel;
  private TransformSurfacePanel transformPanel;
  
  private JFrame frame;
  
  private JPanel selectionPanel;
  private JPanel buttonPanel;
  
  private JPanel editPanel;
  private JTabbedPane editTabPane;
  
  private SceneTreeNode sceneNode;
  
  private JTree surfaceList;
  private JScrollPane surfaceListScrollPane;
  private JButton newButton, removeButton;

	/**
	 * Constructs a new SurfaceInfoPanel using the specified Scene object.
	 */
	public SurfaceInfoPanel(Scene scn) {
		this.scene = scn;
		
		this.sceneNode = new SceneTreeNode(this.scene);
		
		this.selectionPanel = new JPanel(new java.awt.BorderLayout());
		
		this.surfaceList = new JTree(this.sceneNode);
		this.surfaceList.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.surfaceList.setEditable(true);
		// this.surfaceList.setDragEnabled(true);
		this.surfaceList.setCellRenderer(new SurfaceTreeCellRenderer());
		this.surfaceListScrollPane = new JScrollPane(this.surfaceList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.newButton = new JButton("Create New Surface");
		this.removeButton = new JButton("Remove Surface");
		
		this.buttonPanel = new JPanel(new java.awt.GridLayout(2, 1));
		this.buttonPanel.add(this.newButton);
		this.buttonPanel.add(this.removeButton);
		
		this.selectionPanel.add(this.surfaceListScrollPane, java.awt.BorderLayout.CENTER);
		this.selectionPanel.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.editTabPane = new JTabbedPane();
		this.editPanel = new JPanel(new java.awt.CardLayout());
		this.editPanel.add(new JPanel(), "blank");
		this.editPanel.add(this.editTabPane, "edit");
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.selectionPanel, this.editPanel);
		splitPane.setPreferredSize(new java.awt.Dimension(740, 320));
		splitPane.setDividerLocation(175);
		
		this.add(splitPane);
		
		this.surfaceList.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent event) {
				Surface surface = getSelectedSurface();
				
				if (surface == null) {
					((java.awt.CardLayout)editPanel.getLayout()).show(editPanel, "blank");
					return;
				} else {
					((java.awt.CardLayout)editPanel.getLayout()).show(editPanel, "edit");
				}
				
				if (settingsPanel == null) {
					settingsPanel = new EditSurfacePanel((AbstractSurfaceUI)surface);
					settingsPanel.setEventHandler(handler);
				} else {
					settingsPanel.setSurface((AbstractSurfaceUI)surface);
				}
				
				if (editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[0]) < 0) {
					editTabPane.add(settingsPanel, 0);
					editTabPane.setTitleAt(0, SurfaceInfoPanel.editOptions[0]);
				}
				
				AbstractSurface aSurface = null;
				
				if (surface instanceof AbstractSurface)
					aSurface = (AbstractSurface)surface;
				else if (surface instanceof AbstractSurfaceUI)
					aSurface = (AbstractSurface)((AbstractSurfaceUI)surface).getSurface();
				
				if (aSurface != null) {
					if (texturesPanel == null)
						texturesPanel = new ExpandedEditPanel(aSurface.getTextureSet(), new TextureFactory());
					else
						texturesPanel.setEditing(aSurface.getTextureSet());
					
					if (editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[1]) < 0)
						editTabPane.add(SurfaceInfoPanel.editOptions[1], texturesPanel);
					
					if (shadersPanel == null)
						shadersPanel = new ShaderEditPanel(aSurface.getShaderSet(), new ShaderFactory());
					else
						shadersPanel.setEditing(aSurface.getShaderSet());
					
					((ShaderEditPanel)shadersPanel).setSurface(aSurface);
					
					if (editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[2]) < 0)
						editTabPane.add(SurfaceInfoPanel.editOptions[2], shadersPanel);
					
					if (transformPanel == null)
						transformPanel = new TransformSurfacePanel(aSurface);
					else
						transformPanel.setSurface(aSurface);
					
					if (editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[3]) < 0)
						editTabPane.add(SurfaceInfoPanel.editOptions[3], transformPanel);
				} else {
					int tabIndex = editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[1]);
					if (tabIndex >= 0) editTabPane.remove(tabIndex);
					
					tabIndex = editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[2]);
					if (tabIndex >= 0) editTabPane.remove(tabIndex);
					
					tabIndex = editTabPane.indexOfTab(SurfaceInfoPanel.editOptions[3]);
					if (tabIndex >= 0) editTabPane.remove(tabIndex);
				}
			}
		});
		
		this.surfaceList.getModel().addTreeModelListener(new javax.swing.event.TreeModelListener() {
			public void treeNodesChanged(javax.swing.event.TreeModelEvent event) {
				MutableTreeNode node = (MutableTreeNode)event.getTreePath().getLastPathComponent();
				
				if (event.getChildIndices() != null)
					node = (MutableTreeNode)node.getChildAt(event.getChildIndices()[0]);
			}
			
			public void treeNodesInserted(javax.swing.event.TreeModelEvent event) {}
			public void treeNodesRemoved(javax.swing.event.TreeModelEvent event) {}
			public void treeStructureChanged(javax.swing.event.TreeModelEvent event) {}
		});
		
		this.newButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (scene == null)
					return;
				
				SurfaceGroup group = getSelectedGroup();
				
				NewSurfaceDialog newDialog;
				
				if (group != null)
					newDialog = new NewSurfaceDialog(group);
				else
					newDialog = new NewSurfaceDialog(scene);
				
				newDialog.setEventHandler(handler);
				newDialog.showDialog();
			}
		});
		
		this.removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				Surface surface = null;
				boolean removed = false;
				
				MutableTreeNode selectedNode = (MutableTreeNode)surfaceList.getLastSelectedPathComponent();
				
				if (selectedNode == null)
					return;
				
				if (selectedNode instanceof SurfaceTreeNode) {
					SurfaceTreeNode surfaceNode = (SurfaceTreeNode)selectedNode;
					MutableTreeNode parentNode = (MutableTreeNode)surfaceNode.getParent();
					
					surface = surfaceNode.getSurface();
					
					if (parentNode instanceof SceneTreeNode) {
						SceneTreeNode sceneParentNode = (SceneTreeNode)parentNode;
						
						i: for(int i = 0; i < sceneParentNode.getScene().getSurfaces().length; i++) {
							if (sceneParentNode.getScene().getSurface(i) == surface) {
								sceneParentNode.getScene().removeSurface(i);
								removed = true;
								
								break i;
							}
						}
					} else if (parentNode instanceof SurfaceTreeNode) {
						SurfaceTreeNode surfaceParentNode = (SurfaceTreeNode)parentNode;
						SurfaceGroup group = (SurfaceGroup)surfaceParentNode.getSurface();
						
						i: for(int i = 0; i < group.getSurfaces().length; i++) {
							if (group.getSurface(i) == surface) {
								group.removeSurface(i);
								removed = true;
								
								break i;
							}
						}
					}
				}
				
				if (handler != null && removed == true) {
					handler.fireEvent(new SurfaceRemoveEvent(surface));
				}
			}
		});
				
		this.frame = new JFrame("Surface Info");
		this.frame.setSize((int)(splitPane.getPreferredSize().getWidth() + 25), (int)(splitPane.getPreferredSize().getHeight() + 40));
		
		this.frame.getContentPane().add(this);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closePanel();
			}
		});
	}
	
	/**
	 * Shows this panel in a JFrame.
	 */
	public void showPanel() {
		if (this.open == true) {
			this.frame.toFront();
		} else {
			this.updateSurfaceList(true);
			
			this.frame.setVisible(true);
			this.open = true;
		}
	}
	
	/**
	 * Closes this panel if it is open.
	 */
	public void closePanel() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
		}
	}
	
	/**
	 * Returns the currently selected Surface object. If nothing is selected, null is returned.
	 */
	public Surface getSelectedSurface() {
		MutableTreeNode node = (MutableTreeNode)this.surfaceList.getLastSelectedPathComponent();
		
		if (node == null || !(node instanceof SurfaceTreeNode))
			return null;
		
		Surface surface = ((SurfaceTreeNode)node).getSurface();
		
		return surface;
	}
	
	/**
	 * Returns the currently selected SurfaceGroup object or the SurfaceGroup object containing
	 * the currently selected Surface object. If nothing is selected, null is returned.
	 */
	public SurfaceGroup getSelectedGroup() {
		MutableTreeNode node = (MutableTreeNode)this.surfaceList.getLastSelectedPathComponent();
		Surface surface = this.getSelectedSurface();
		
		if (surface == null) {
			return null;
		} else if (surface instanceof SurfaceGroup) {
			return (SurfaceGroup)surface;
		} else if (surface instanceof SurfaceUI) {
			Surface s = ((SurfaceUI)surface).getSurface();
			if (s instanceof SurfaceGroup) return (SurfaceGroup) s;
		}
		
		MutableTreeNode parent = (MutableTreeNode)node.getParent();
		
		if (parent == null || !(parent instanceof SurfaceTreeNode))
			return null;
		
		Surface parentSurface = ((SurfaceTreeNode)parent).getSurface();
		
		if (parentSurface instanceof SurfaceGroup)
			return (SurfaceGroup)parentSurface;
		else
			return null;
	}
	
	/**
	 * Updates the Surface list of this panel.
	 */
	public void updateSurfaceList(boolean structureChanged) {
		if (this.scene == null)
			this.surfaceList.setRootVisible(false);
		else
			this.surfaceList.setRootVisible(true);
		
		if (structureChanged == true) {
			//TreeNode node = (TreeNode)this.surfaceList.getLastSelectedPathComponent();
			
			((javax.swing.tree.DefaultTreeModel)this.surfaceList.getModel()).nodeStructureChanged((MutableTreeNode)this.surfaceList.getModel().getRoot());
			
			//javax.swing.tree.TreeNode path[] = ((javax.swing.tree.DefaultTreeModel)this.surfaceList.getModel()).getPathToRoot(node);
			//if (path != null && path.length > 0)
			//	this.surfaceList.setSelectionPath(new javax.swing.tree.TreePath(path));
		}
		
		this.surfaceList.revalidate();
		this.surfaceList.repaint();
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			SceneOpenEvent openEvent = (SceneOpenEvent)event;
			this.scene = openEvent.getScene();
			
			this.updateSurfaceList(true);
			return;
		}
		
		if (event instanceof SceneCloseEvent) {
			this.scene = null;
			
			this.closePanel();
			return;
		}
		
		if (event instanceof SurfaceEditEvent && (((SurfaceEditEvent)event).isNameChangeEvent() == true || ((SurfaceEditEvent)event).isColorChangeEvent() == true)) {
			this.updateSurfaceList(true);
			return;
		}
		
		if (event instanceof SurfaceAddEvent || event instanceof SurfaceRemoveEvent) {
			this.updateSurfaceList(true);
			return;
		}
	}
	
	/**
	 * Sets the EventHandler object used by this SurfaceInfoPanel object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.sceneNode.setEventHandler(this.handler);
		
		if (this.settingsPanel != null)
			this.settingsPanel.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.sceneNode);
		}
	}
	
	/**
	 * @return  The EventHandler object used by this SurfaceInfoPanel object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
