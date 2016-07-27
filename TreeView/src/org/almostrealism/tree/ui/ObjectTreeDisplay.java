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

package org.almostrealism.tree.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

import org.almostrealism.obj.ObjectFactory;
import org.almostrealism.ui.panels.PercentagePanel;

import com.almostrealism.photonfield.util.VectorMath;

/**
 * @author Mike Murray
 */
public class ObjectTreeDisplay extends JPanel implements ActionListener,
														MouseListener,
														TreeSelectionListener,
														ChangeListener {
	private JPanel treePanel, rPanel, propPanel;
	private JPanel buttonPanel;
	private PercentagePanel perPanel;
	
	private ObjectTreeNode root;
	private JTree tree;
	private JButton addButton, upButton, downButton;
	private JPopupMenu addMenu;
	private JMenu addMethodMenu, addObjectMenu;
	
	private Hashtable types, methods, panelTypes, panels, configTypes;
	
	public ObjectTreeDisplay(ObjectTreeNode root) {
		super(new BorderLayout());
		
		this.types = new Hashtable();
		this.methods = new Hashtable();
		this.panelTypes = new Hashtable();
		this.panels = new Hashtable();
		this.configTypes = new Hashtable();
		
		this.root = root;
		this.tree = new JTree(root);
		this.tree.addTreeSelectionListener(this);
		
		this.addMenu = new JPopupMenu();
		this.addMethodMenu = new JMenu("Method");
		this.addObjectMenu = new JMenu("Object");
		this.addMenu.add(this.addMethodMenu);
		this.addMenu.add(this.addObjectMenu);
		
		this.upButton = new JButton("Up");
		this.downButton = new JButton("Down");
		this.addButton = new JButton("+");
		this.addButton.addMouseListener(this);
		
		this.buttonPanel = new JPanel();
		this.buttonPanel.add(this.addButton);
		this.buttonPanel.add(this.upButton);
		this.buttonPanel.add(this.downButton);
		
		this.treePanel = new JPanel(new BorderLayout());
		this.treePanel.add(this.tree, BorderLayout.CENTER);
		this.treePanel.add(this.buttonPanel, BorderLayout.SOUTH);
		this.treePanel.setPreferredSize(new Dimension(100, 300));
		
		this.propPanel = new JPanel(new BorderLayout());
		this.perPanel = new PercentagePanel();
		this.perPanel.addChangeListener(this);
		this.propPanel.setPreferredSize(new Dimension(100, 300));
		
		this.rPanel = new JPanel(new BorderLayout());
		this.rPanel.add(this.propPanel, BorderLayout.CENTER);
		this.rPanel.add(this.perPanel, BorderLayout.SOUTH);
		
		super.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.treePanel, this.rPanel));
	}
	
	public ObjectTreeNode getRoot() { return this.root; }
	public Object getRootObject() throws IllegalArgumentException,
										IllegalAccessException,
										InvocationTargetException,
										InstantiationException {
		return this.root.getObject();
	}
	
	public void setTarget(Object target) { this.root.setTarget(target); }
	
	public void addObjectType(Class c) {
		String n = c.getName();
		JMenuItem item = new JMenuItem(n.substring(n.lastIndexOf(".") + 1));
		item.addActionListener(this);
		this.addObjectMenu.add(item);
		this.types.put(item, c);
	}
	
	public void addMethodType(Method m) {
		JMenuItem item = new JMenuItem(m.getName());
		item.addActionListener(this);
		this.addMethodMenu.add(item);
		this.types.put(item, m.getClass());
	}
	
	public void addEditPanelType(Class objClass, Class panelClass) {
		this.panelTypes.put(objClass, panelClass);
	}
	
	public void addConfigurationDialogType(Class objClass, Class dialogClass) {
		this.configTypes.put(objClass, dialogClass);
	}
	
	public void setBackground(Color c) {
		if (this.tree == null)
			super.setBackground(c);
		else
			this.tree.setBackground(c);
	}
	
	public void setTreeCellRenderer(TreeCellRenderer r) { this.tree.setCellRenderer(r); }
	
	public void actionPerformed(ActionEvent e) {
		try {
			ObjectTreeNode p = this.getSelected();
			
			if (e.getSource() == this.upButton) {
			} else if (e.getSource() == this.downButton) {
			} else if (this.types.containsKey(e.getSource())) {
				Class c = (Class) this.types.get(e.getSource());
				
				if (this.configTypes.containsKey(c)) {
					Class dc = (Class) this.configTypes.get(c);
					Object o = dc.newInstance();
					
					if (o instanceof ObjectFactory) {
						ObjectFactory f = (ObjectFactory) o;
						if (JOptionPane.showConfirmDialog(this, o, "Add " + c.getName(),
								JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION)
							p.addChildObject(f.newInstance());
					} else {
						p.addChildObject(c.newInstance());
					}
				} else {
					p.addChildObject(c.newInstance());
				}
			} else if (this.methods.containsKey(e.getSource())) {
				Class c = (Class) this.methods.get(e.getSource());
				p.addChildMethod((Method) c.newInstance());
			} else {
				return;
			}
			
			((DefaultTreeModel)this.tree.getModel()).nodeStructureChanged(p);
		} catch (InstantiationException ie) {
			System.out.println("ObjectTreeDisplay: Error constructing node object (" +
								ie.getCause().getMessage() + ")");
		} catch (IllegalAccessException iae) {
			System.out.println("ObjectTreeDisplay: Illegal access to node object type (" +
								iae.getMessage() + ")");
		}
	}
	
	public ObjectTreeNode getSelected() {
		if (this.tree.getLastSelectedPathComponent() != null)
			return (ObjectTreeNode) this.tree.getLastSelectedPathComponent();
		else
			return (ObjectTreeNode) this.tree.getModel().getRoot();
	}
	
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	
	public void mousePressed(MouseEvent e) {
		this.addMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	public void updateDisplay() {
		((DefaultTreeModel)this.tree.getModel()).nodeStructureChanged(this.getSelected());
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		ObjectTreeNode n = (ObjectTreeNode) e.getPath().getLastPathComponent();
		this.perPanel.setValue(n.getMultiplier());
		
		Object c = this.panels.get(n);
		
		try {
			Object o = n.getObject();
			
			c: if (c == null && o != null && this.panelTypes.containsKey(o.getClass())) {
				Class cl = (Class) this.panelTypes.get(o.getClass());
				if (cl == null) break c;
				
				Constructor con = cl.getConstructor(new Class[] {o.getClass()});
				
				if (con == null)
					c = cl.newInstance();
				else
					c = con.newInstance(new Object[] {o});
				
				Method m = cl.getMethod("setChangeListener",
								new Class[] { ChangeListener.class });
				if (m != null) m.invoke(c, new Object[] { this });
				this.panels.put(n, c);
			}
		} catch (Exception ex) {
			System.out.println("ObjectTreeDisplay: " + ex);
		}
		
		if (this.propPanel.getComponentCount() > 0) this.propPanel.remove(0);
		if (c != null && c instanceof Component) this.propPanel.add((Component) c);
		this.propPanel.validate();
	}
	
	public void stateChanged(ChangeEvent e) {
		this.getSelected().setMultiplier(this.perPanel.getValue());
		this.tree.repaint();
	}
	
	public static void main(String args[]) throws SecurityException, NoSuchMethodException {
		Method m =
			VectorMath.class.getMethod("add", new Class[] {double[].class, double[].class});
		
		ObjectTreeDisplay d = new ObjectTreeDisplay(new ObjectTreeNode(null, m));
		d.addMethodType(m);
		d.addObjectType(double[].class);
		
		JFrame frame = new JFrame("ObjectTreeDisplay Demo");
		frame.getContentPane().add(d);
		frame.setVisible(true);
	}
}
