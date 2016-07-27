package net.sf.glitchfarm.transform.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.glitchfarm.gui.SampleDisplayPane;
import net.sf.glitchfarm.gui.util.Transformers;
import net.sf.glitchfarm.obj.Sample;
import net.sf.glitchfarm.transform.SampleTransformer;
import net.sf.glitchfarm.util.LineUtilities;

public class TransformerTabbedPane extends JPanel implements ActionListener {
	private Hashtable<Component, SampleTransformer> transformers;
	
	private Sample sample;
	
	private JTabbedPane tabs;
	private JButton okButton, cancelButton, updateButton;
	private ActionListener doneListener;
	
	private SampleDisplayPane previewPanel;
	
	public TransformerTabbedPane(Sample s) {
		super(new BorderLayout());
		
		transformers = new Hashtable<Component, SampleTransformer>();
		
		this.tabs = new JTabbedPane();
		
		this.sample = s;
		
		Iterator<SampleTransformer> itr = Transformers.available.iterator();
		while (itr.hasNext()) {
			SampleTransformer st = itr.next();
			Component c = Transformers.createEditor(st);
			transformers.put(c, st);
			this.tabs.addTab(st.toString(), c);
		}
		
		this.okButton = new JButton("Ok");
		this.cancelButton = new JButton("Cancel");
		this.updateButton = new JButton("Update Preview");
		
		this.okButton.addActionListener(this);
		this.cancelButton.addActionListener(this);
		this.updateButton.addActionListener(this);
		
		this.previewPanel = new SampleDisplayPane(s, LineUtilities.getLine(), true);
		this.previewPanel.setDisplayHeight(100);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.updateButton);
		buttonPanel.add(this.cancelButton);
		buttonPanel.add(this.okButton);
		
		super.add(this.previewPanel, BorderLayout.NORTH);
		super.add(this.tabs, BorderLayout.CENTER);
		super.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Set the action listener that will be notified when the user is "done"
	 * with this TransformTabbedPane.
	 */
	public void setDoneListener(ActionListener a) {
		this.doneListener = a;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.cancelButton && this.doneListener != null) {
			this.doneListener.actionPerformed(e);
		} else if (e.getSource() == this.okButton) {
			this.transformers.get(this.tabs.getSelectedComponent()).transform(this.sample);
			this.doneListener.actionPerformed(e);
		} else if (e.getSource() == this.updateButton) {
			this.sample.stop();
			Sample s = this.sample.deepCopy();
			this.transformers.get(this.tabs.getSelectedComponent()).transform(s);
			this.previewPanel.setSample(s);
		}
	}
}
