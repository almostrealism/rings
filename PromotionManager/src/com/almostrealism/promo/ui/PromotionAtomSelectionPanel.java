package com.almostrealism.promo.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.almostrealism.promo.entities.Material;
import com.almostrealism.promo.entities.PromotionAtom;
import com.almostrealism.promo.persist.PromotionDatabase;
import com.almostrealism.promo.services.PromotionAtomFactory;

/**
 * The {@link PromotionAtomSelectionPanel} displays a list of {@link PromotionAtom}s
 * for each {@link Material} instance found in the {@link PromotionDatabase} in a
 * tree format.
 * 
 * @author  Michael Murray
 */
public class PromotionAtomSelectionPanel extends JPanel implements TreeModel {
	private String rootNodeLabel = "Material";
	
	private PromotionDatabase db;
	private PromotionAtomFactory factory;
	
	private JTree tree;
	private ActionListener useButtonListener;
	
	/**
	 * Constructs a new selection panel using the specified {@link PromotionDatabase}
	 * along with the specified {@link PromotionAtomFactory} for generating the atoms
	 * for the material in the database.
	 */
	public PromotionAtomSelectionPanel(PromotionDatabase db, PromotionAtomFactory f) {
		super(new BorderLayout());
		
		this.db = db;
		this.factory = f;
		
		JButton useButton = new JButton("Use");
		useButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (useButtonListener != null)
					useButtonListener.actionPerformed(e);
			}
		});
		
		this.tree = new JTree(this);
		this.add(new JScrollPane(tree), BorderLayout.CENTER);
		this.add(useButton, BorderLayout.SOUTH);
	}
	
	/**
	 * Returns the {@link PromotionAtom} that is currently selected,
	 * if there is one.
	 */
	public PromotionAtom getSelectedAtom() {
		if (this.tree.getSelectionModel().getLeadSelectionPath() == null) return null;
		
		Object path[] = this.tree.getSelectionModel().getLeadSelectionPath().getPath();
		
		if (path[path.length - 1] instanceof PromotionAtom)
			return ((PromotionAtom) path[path.length - 1]);
		else
			return null;
	}
	
	/**
	 * Set the {@link ActionListener} that will be notified when a selected
	 * {@link PromotionAtom} is tapped for use.
	 */
	public void setUseButtonListener(ActionListener l) { this.useButtonListener = l; }
	
	/**
	 * Returns a String label for the root node.
	 */
	public Object getRoot() { return rootNodeLabel; }
	
	/**
	 * Returns either a {@link Material} instance or a {@link PromotionAtom} instance.
	 */
	public Object getChild(Object parent, int index) {
		if (parent.equals(rootNodeLabel)) {
			List<Material> m = (List<Material>) db.getMaterial();
			return m.get(index);
		} else if (parent instanceof Material) {
			return ((List<PromotionAtom>)this.factory.getAtoms((Material) parent)).get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the number of children for the specified node.
	 */
	public int getChildCount(Object parent) {
		if (parent.equals(rootNodeLabel)) {
			return db.getMaterial().size();
		} else if (parent instanceof Material) {
			return this.factory.getAtoms((Material) parent).size();
		} else {
			return 0;
		}
	}
	
	/**
	 * Returns true for the {@link PromotionAtom} leaf nodes
	 * in the tree.
	 */
	public boolean isLeaf(Object node) {
		if (node.equals(rootNodeLabel)) {
			return false;
		} else if (node instanceof Material) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}
}
