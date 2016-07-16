package com.almostrealism.promotions.services;

import java.util.Collection;

import com.almostrealism.promotions.analysis.AnalysisNews;
import com.almostrealism.promotions.entities.PromotionMolecule;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.ui.EngagementEditor;

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
