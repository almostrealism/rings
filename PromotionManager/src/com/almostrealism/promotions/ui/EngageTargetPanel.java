package com.almostrealism.promotions.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.almostrealism.promotions.entities.Material;
import com.almostrealism.promotions.entities.PromotionAtom;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.persist.PromotionDatabase;
import com.almostrealism.promotions.services.EngagementService;
import com.almostrealism.promotions.services.PromotionAtomFactory;

/**
 * The {@link EngageTargetPanel} provides an abstraction for all types of
 * {@link EngagementService}s which are available in the promotions system.
 * This panel provides the user with the opportunity to design an engagement
 * for a particular {@link Target} using any available {@link PromotionAtom}s
 * for the {@link Material} which is being promoted.
 * 
 * @author  Michael Murray
 */
public class EngageTargetPanel extends JPanel {
	private EngagementService service;
	private Target target;
	
	private EngagementEditor editor;
	private PromotionAtomSelectionPanel atomPanel;
	
	/**
	 * Create a new {@link EngageTargetPanel} for the specified {@link Target}
	 * using the specified {@link EngagementService}. The specified
	 * {@link PromotionAtomFactory} will be used to get {@link PromotionAtom}s
	 * for the {@link Material} in the specified {@link PromotionDatabase}.
	 */
	public EngageTargetPanel(PromotionDatabase db, PromotionAtomFactory factory, EngagementService service, Target t) {
		super(new BorderLayout());
		
		this.service = service;
		this.target = t;
		
		this.editor = service.getEditorPanel(target);
		
		this.atomPanel = new PromotionAtomSelectionPanel(db, factory);
		this.atomPanel.setUseButtonListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.addPromotionAtom(atomPanel.getSelectedAtom());
			}
		});
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, atomPanel);
		this.add(split);
	}
}
