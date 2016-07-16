package com.almostrealism.promotions.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.almostrealism.promotions.entities.PromotionAtom;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.entities.Target.TargetType;
import com.almostrealism.promotions.persist.PromotionDatabase;
import com.almostrealism.promotions.services.EngagementService;
import com.almostrealism.promotions.services.PromotionAtomFactory;

/**
 * The {@link TargetsPanel} displays a table with information
 * about each target that is part of the promotional campaign. Some
 * fields in the table are modifiable, and these changes do side effect
 * the {@link Target}s that are provided when initializing the panel.
 * 
 * The {@link TargetsPanel} also provides quick access to the
 * functions of the {@link EngageTargetPanel}, so that {@link Target}s
 * can be engaged via any available {@link EngagementService}.
 * 
 * @author  Michael Murray
 */
public class TargetsPanel extends JPanel implements TableModel {
	private PromotionDatabase db;
	private PromotionAtomFactory factory;
	
	private JTable targetsTable;
	
	/**
	 * Construct a new {@link TargetsPanel}. A {@link PromotionDatabase}
	 * is required to provide the data displayed on the panel, and the panel may
	 * side effect the data contained there if changes are made by the user.
	 * A {@link PromotionAtomFactory} is required to generate {@link PromotionAtom}s
	 * when engaging a {@link Target}.
	 */
	public TargetsPanel(PromotionDatabase db, PromotionAtomFactory factory) {
		super(new BorderLayout());
		
		this.db = db;
		this.factory = factory;
		this.targetsTable = new JTable(this);
		
		this.add(new JScrollPane(this.targetsTable));
	}
	
	/**
	 * Returns an {@link Action} which can be added to a menu, button
	 * or other user interface component to allow the user to add a
	 * new {@link Target} to the {@link PromotionDatabase}.
	 */
	public Action getAddTargetAction() {
		return new AbstractAction("Add Target...") {
			public void actionPerformed(ActionEvent e) {
				JComboBox typeBox = new JComboBox(new String[] {"Blog", "Label", "Performer"});
				String s = JOptionPane.showInputDialog(TargetsPanel.this, typeBox, "Target Type",
														JOptionPane.PLAIN_MESSAGE);
				
				if (s == null) return;
				
				TargetType type = TargetType.Blog;
				if (typeBox.getSelectedIndex() == 1) type = TargetType.Label;
				if (typeBox.getSelectedIndex() == 2) type = TargetType.Performer;
				
				Target t = new Target();
				t.setName(s);
				t.setType(type);
				
				db.getTargets().add(t);
				targetsTable.tableChanged(new TableModelEvent(TargetsPanel.this));
			}
		};
	}
	
	/**
	 * Returns an {@link Action} which can be added to a menu, button
	 * or other user interface component to allow the user to engage a
	 * selected {@link Target} using an {@link EngagementService}.
	 */
	public Action getEngageTargetAction() {
		return new AbstractAction("Engage Target...") {
			public void actionPerformed(ActionEvent e) {
				Target t = ((List<Target>) db.getTargets()).get(targetsTable.getSelectedRow());
				Vector<EngagementService> services = new Vector<EngagementService>();
				services.addAll(db.getServices());
				
				JComboBox b = new JComboBox(services);
				JOptionPane.showMessageDialog(TargetsPanel.this, b, "Select Service", JOptionPane.PLAIN_MESSAGE);
				
				if (b.getSelectedItem() == null) return;
				
				EngageTargetPanel panel = new EngageTargetPanel(db, factory, (EngagementService) b.getSelectedItem(), t);
				
				JFrame f = new JFrame("Engage " + t.getName());
				f.getContentPane().add(panel);
				f.setSize(200, 300);
				f.setLocation(400, 400);
				f.setVisible(true);
			}
		};
	}
	
	/**
	 * Returns the number of targets
	 */
	public int getRowCount() { return db.getTargets().size(); }
	
	/**
	 * Returns 6
	 */
	public int getColumnCount() { return 6; }
	
	/**
	 * Column names are "Type", "Name", "Contact Email", "Contact URL", "Consumer URL", "Notes"
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0: return "Type";
			case 1: return "Name";
			case 2: return "Contact Email";
			case 3: return "Contact URL";
			case 4: return "Consumer URL";
			case 5: return "Notes";
		}
		
		return "";
	}
	
	/**
	 * All columns are of type {@link String}, except for the first column
	 * for type which is a {@link TargetType} instance.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return TargetType.class;
		else
			return String.class;
	}
	
	/**
	 * Returns true. All cells are editable.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) { return true; }
	
	/**
	 * Returns a value for the specified column.
	 * 
	 * @see  #getColumnName(int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Target t = ((List<Target>) db.getTargets()).get(rowIndex);
		
		switch (columnIndex) {
			case 0: return t.getType();
			case 1: return t.getName();
			case 2: return t.getContactEmail();
			case 3: return t.getContactUrl();
			case 4: return t.getConsumerUrl();
			case 5: return t.getNotes();
		}
		
		return null;
	}
	
	/**
	 * Sets a particular value for the {@link Target} located at the specified row.
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Target t = ((List<Target>) db.getTargets()).get(rowIndex);
		
		switch (columnIndex) {
			case 0: t.setType((TargetType) aValue); break;
			case 1: t.setName((String) aValue); break;
			case 2: t.setContactEmail((String) aValue); break;
			case 3: t.setContactUrl((String) aValue); break;
			case 4: t.setConsumerUrl((String) aValue); break;
			case 5: t.setNotes((String) aValue); break;
		}
	}
	
	public void addTableModelListener(TableModelListener l) { }
	public void removeTableModelListener(TableModelListener l) { }
}
