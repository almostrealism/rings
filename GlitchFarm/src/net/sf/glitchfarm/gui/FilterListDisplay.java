package net.sf.glitchfarm.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.glitchfarm.filter.AmplitudeRangeFilter;
import net.sf.glitchfarm.filter.BlurFilter;
import net.sf.glitchfarm.filter.LineFilter;
import net.sf.glitchfarm.filter.SineFilter;
import net.sf.glitchfarm.line.FilterOutputLine;
import net.sf.glitchfarm.line.OutputLine;
import net.sf.glitchfarm.util.DataProducer;
import net.sf.glitchfarm.util.DataReceiver;

/**
 * The FilterListDisplay displays a list of filters, in order, and patches them
 * together when used as an output line or a data producer/receiver.
 */
public class FilterListDisplay extends JPanel implements OutputLine,
												DataProducer,
												DataReceiver,
												ActionListener,
												ListSelectionListener {
	private OutputLine line;
	private byte next[];
	
	private JList list;
	private JButton addButton;
	private JComboBox filterComboBox;
	
	private JSplitPane splitPane;
	
	public FilterListDisplay() {
		super(new BorderLayout());
		
		this.filterComboBox = new JComboBox(new Object[] {"BlurFilter",
														"SineFilter",
														"AmplitudeRangeFilter"});
		
		this.addButton = new JButton("Add...");
		this.addButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.addButton);
		
		this.list = new JList();
		this.list.addListSelectionListener(this);
		this.list.setModel(new DefaultListModel());
		
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(this.list, BorderLayout.CENTER);
		listPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, new JPanel());
		
		super.add(this.splitPane);
	}
	
	public FilterListDisplay(OutputLine top) {
		this();
		this.line = top;
	}
	
	public OutputLine getLine() { return this; }

	public void write(byte b[]) {
		this.line.write(b);
	}
	
	public void next(byte b[]) {
		this.next = b;
		
		Enumeration en = ((DefaultListModel)this.list.getModel()).elements();
		
		while (en.hasMoreElements()) {
			FilterOutputLine f = ((FilterListItem) en.nextElement()).getLine();
			f.next(this.next);
			this.next = f.next();
		}
	}
	
	public byte[] next() {
		byte b[] = this.next;
		this.next = null;
		return b;
	}
	
	protected LineFilter createFilter(int index) {
		if (index == 0)
			return new BlurFilter();
		else if (index == 1)
			return new SineFilter();
		else if (index == 2)
			return new AmplitudeRangeFilter();
		else
			return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.addButton) {
			JOptionPane.showMessageDialog(null, this.filterComboBox, "Filter", JOptionPane.INFORMATION_MESSAGE);
			
			LineFilter f = createFilter(this.filterComboBox.getSelectedIndex());
			this.line = new FilterOutputLine(f, this.line);
			
			FilterListItem item = new FilterListItem((FilterOutputLine) line, f);
			((DefaultListModel) this.list.getModel()).add(0, item);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		JPanel p = ((FilterListItem)this.list.getModel().getElementAt(this.list.getSelectedIndex())).getConfigPanel();
		this.splitPane.setRightComponent(p);
		this.validate();
		if (this.getParent() instanceof Frame) ((Frame)this.getParent()).pack();
		this.repaint();
	}
}
