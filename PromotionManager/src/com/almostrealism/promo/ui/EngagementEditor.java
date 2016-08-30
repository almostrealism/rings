package com.almostrealism.promo.ui;

import javax.swing.JPanel;

import com.almostrealism.promo.entities.PromotionAtom;
import com.almostrealism.promo.entities.PromotionMolecule;

/**
 * {@link EngagementEditor} is the parent class for user interface components which
 * provide support for configuring a {@link PromotionMolecule}. Useful methods are
 * provided by this class, but no components are added to the panel.
 * 
 * @author  Michael Murray
 */
public abstract class EngagementEditor extends JPanel {
	private PromotionMolecule molecule;
	
	/**
	 * Constructs a new {@link EngagementEditor}. This causes
	 * a call to the {@link #init()} method.
	 */
	public EngagementEditor() {
		init();
	}
	
	/**
	 * This method is called by the constructor so subclasses can initialize the display.
	 */
	public abstract void init();
	
	/**
	 * Add a {@link PromotionAtom} to the {@link PromotionMolecule} maintained
	 * by this {@link EngagementEditor}.
	 */
	public void addPromotionAtom(PromotionAtom atom) { this.molecule.addAtom(atom); }
	
	/**
	 * Set the {@link PromotionMolecule} instance this panel is editing.
	 */
	public void setMolecule(PromotionMolecule m) { this.molecule = m; }
	
	/**
	 * Return the {@link PromotionMolecule} instance this panel is editing.
	 */
	public PromotionMolecule getMolecule() { return this.molecule; }
}
