package com.almostrealism.promo.entities;

import com.almostrealism.promo.services.EngagementService;

/**
 * Various types of {@link PromotionAtom} implementations provide
 * content for {@link PromotionMolecule}s that are used with
 * {@link EngagementService}s.
 * 
 * @author  Michael Murray
 */
public interface PromotionAtom {
	/**
	 * Return the name for this atom.
	 */
	public String getName();
	
	/**
	 * Return the generated body for this atom.
	 */
	public String getBody();
}