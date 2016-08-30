package com.almostrealism.promotions.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.almostrealism.promotions.entities.Material;
import com.almostrealism.promotions.persist.PromotionDatabase;
import com.almostrealism.promotions.services.EngagementService;

/**
 * The {@link MaterialsPanel} displays a list of the {@link Material} from
 * the {@link PromotionDatabase}, along with some components for organizing
 * and editing {@link Material} meta data to prepare for using an
 * {@link EngagementService}.
 * 
 * @author  Michael Murray
 */
public class MaterialsPanel extends JPanel implements ListSelectionListener {
	private class MaterialListModel extends AbstractListModel<Material> {
		public Material getElementAt(int index) {
			return ((List<Material>) MaterialsPanel.this.db.getMaterial()).get(index);
		}
		
		public int getSize() { return MaterialsPanel.this.db.getMaterial().size(); }
		
	    public void update() {
	        this.fireContentsChanged(this, 0, MaterialsPanel.this.db.getMaterial().size() - 1);
	    }
	}
	
	private PromotionDatabase db;
	
	private JList<Material> materialList;
	private WebPresenceEditorPanel presencePanel;
	
	/**
	 * Creates a new {@link MaterialsPanel} 
	 */
	public MaterialsPanel(PromotionDatabase db) {
		super(new BorderLayout());
		
		this.db = db;
		
		this.materialList = new JList<Material>(new MaterialListModel());
		this.materialList.addListSelectionListener(this);
		
		this.presencePanel = new WebPresenceEditorPanel();
		
		this.add(new JScrollPane(materialList), BorderLayout.WEST);
		this.add(presencePanel, BorderLayout.CENTER);
	}
	
	/**
	 * Returns an {@link Action} which can be added to a menu, button
	 * or other user interface component to allow the user to add a
	 * new {@link Material} item to the {@link PromotionDatabase}.
	 */
	public Action getAddMaterialAction() {
		return new AbstractAction("Add Material...") {
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(MaterialsPanel.this, "Material Name", "Add Material",
														JOptionPane.PLAIN_MESSAGE);
				
				Material m = new Material();
				m.setName(s);
				
				db.getMaterial().add(m);
				((MaterialListModel) materialList.getModel()).update();
			}
		};
	}
	
	/**
	 * Updates the editor to reflect a new selection in the list
	 * of materials.
	 */
	public void valueChanged(ListSelectionEvent e) {
		this.presencePanel.setMaterial(materialList.getSelectedValue());
	}
}
