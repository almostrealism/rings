package com.almostrealism.promo.services;

import java.util.Collection;

import com.almostrealism.promo.analysis.AnalysisNews;
import com.almostrealism.promo.entities.PromotionMolecule;
import com.almostrealism.promo.entities.Target;
import com.almostrealism.promo.ui.EngagementEditor;

/**
 * {@link EngagementService} implementations provide a particular
 * way of engaging the promotion audience.
 * 
 * @author  Michael Murray
 */
public interface EngagementService {
	/**
	 * Return an editor for the {@link PromotionMolecule} used by this service.
	 */
	public EngagementEditor getEditorPanel(Target t);
	
	/**
	 * Refresh this service and return any news.
	 */
	public Collection<AnalysisNews> refresh();
}
