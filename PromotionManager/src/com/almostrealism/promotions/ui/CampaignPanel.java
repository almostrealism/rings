package com.almostrealism.promotions.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.almostrealism.promotions.entities.Material;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.persist.PromotionDatabase;
import com.almostrealism.promotions.services.PromotionAtomFactory;

/**
 * The {@link CampaignPanel} displays three tabs, one for each of the
 * operations of the campaign. These are {@link Material}, {@link Target}s,
 * and analytics.
 * 
 * @see  TargetsPanel
 * 
 * @author  Michael Murray
 */
public class CampaignPanel extends JPanel {
	private JTabbedPane tabs;
	
	private MaterialsPanel materialsPanel;
	private TargetsPanel targetsPanel;
	
	/**
	 * Constructs a new {@link CampaignPanel}.
	 */
	public CampaignPanel(PromotionDatabase db) {
		super(new BorderLayout());
		
		this.materialsPanel = new MaterialsPanel(db);
		this.targetsPanel = new TargetsPanel(db, new PromotionAtomFactory());
		
		this.tabs = new JTabbedPane();
		this.tabs.addTab("Material", materialsPanel);
		this.tabs.addTab("Targets", targetsPanel);
		this.tabs.addTab("Analysis", new JPanel());
		
		this.add(this.tabs, BorderLayout.CENTER);
	}
	
	public TargetsPanel getTargetsPanel() { return targetsPanel; }
	
	public MaterialsPanel getMaterialsPanel() { return materialsPanel; }
}
