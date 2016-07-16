package com.almostrealism.promotions.entities;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.promotions.services.EngagementService;

/**
 * A {@link PromotionMolecule} is a collection of {@link PromotionAtom}s
 * which will be used together in a particular way. The {@link PromotionMolecule}
 * is the basis for all {@link EngagementService}s.
 * 
 * @author  Michael Murray
 */
public abstract class PromotionMolecule {
	private List<PromotionAtom> atoms;
	
	/**
	 * Create a new empty {@link PromotionMolecule}.
	 */
	public PromotionMolecule() {
		this.atoms = new ArrayList<PromotionAtom>();
	}
	
	/**
	 * Add a {@link PromotionAtom} to the molecule.
	 */
	public void addAtom(PromotionAtom atom) { this.atoms.add(atom); }
	
	/**
	 * Returns the {@link PromotionAtom}s for this molecule.
	 * Changes to the list will side effect this molecule.
	 */
	public List<PromotionAtom> getAtoms() { return this.atoms; }
}
