package net.sf.glitchfarm.gui.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.glitchfarm.gui.BeatBoxDisplay;
import net.sf.glitchfarm.line.DefaultDualOutputLine;
import net.sf.glitchfarm.line.MultiplierDualOutputLine;
import net.sf.glitchfarm.line.OutputLine;
import net.sf.glitchfarm.line.gui.DualOutputLineMixPanel;
import net.sf.glitchfarm.mixer.Mixer;
import net.sf.glitchfarm.util.DataProducer;
import net.sf.glitchfarm.util.DataReceiver;
import net.sf.glitchfarm.util.LineUtilities;

public class TabbedMixView extends JPanel implements ActionListener {
	private DualOutputLineMixPanel mixPanel;
	private DefaultDualOutputLine line;
	private OutputLine leftFilter, rightFilter;
	
	private JTabbedPane leftTabs, rightTabs;
	private JButton playButton, leftFilterButton, rightFilterButton;
	
	private Thread playThread;
	
	public TabbedMixView() {
		super(new BorderLayout());
		this.line = new DefaultDualOutputLine(LineUtilities.getLine());
		this.line = new MultiplierDualOutputLine(LineUtilities.getLine());
		this.mixPanel = new DualOutputLineMixPanel(this.line);
		
		this.leftTabs = new JTabbedPane();
		this.rightTabs = new JTabbedPane();
		
		this.addLeftTab();
		this.addRightTab();
		
//		this.getCurrentLeftBeatBox().setOutputLine(this.mixPanel.getLeftLine());
//		this.getCurrentRightBeatBox().setOutputLine(this.mixPanel.getRightLine());
		
		this.playButton = new JButton("Play");
		this.leftFilterButton = new JButton("Left Line");
		this.rightFilterButton = new JButton("Right Line");
		
		this.playButton.addActionListener(this);
		this.leftFilterButton.addActionListener(this);
		this.rightFilterButton.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(this.leftFilterButton);
		bottomPanel.add(this.mixPanel);
		bottomPanel.add(this.rightFilterButton);
		bottomPanel.add(this.playButton);
		
		this.add(this.leftTabs, BorderLayout.WEST);
		this.add(this.rightTabs, BorderLayout.EAST);
		this.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	public BeatBoxDisplay getCurrentLeftBeatBox() {
		StandardMixView c = (StandardMixView) this.leftTabs.getSelectedComponent();
		return (BeatBoxDisplay) c.getLeft();
	}
	
	public BeatBoxDisplay getCurrentRightBeatBox() {
		StandardMixView c = (StandardMixView) this.rightTabs.getSelectedComponent();
		return (BeatBoxDisplay) c.getLeft();
	}
	
	
	
	public void addLeftTab() { this.addTab(true); }	
	public void addRightTab() { this.addTab(false); }
	
	private void addTab(boolean left) {
		Component c = this.createTableComponent();
		
		if (left)
			this.leftTabs.addTab(c.toString(), c);
		else
			this.rightTabs.addTab(c.toString(), c);
	}
	
	public Component createTableComponent() {
		return new BeatBoxDisplay(LineUtilities.getAudioFormat());
	}
	
	public Thread playThread() {
		final DataProducer left = this.getCurrentLeftBeatBox();
		final DataProducer right = this.getCurrentRightBeatBox();
		final OutputLine leftLine = this.mixPanel.getLeftLine();
		final OutputLine rightLine = this.mixPanel.getRightLine();
		
		return new Thread() {
			public void run() {
				while (true) {
					byte l[] = left.next();
					byte r[] = right.next();
					
					if (leftFilter != null) {
						((DataReceiver)leftFilter).next(l);
						l = ((DataProducer)leftFilter).next();
					}
					
					if (rightFilter != null) {
						((DataReceiver)rightFilter).next(r);
						r = ((DataProducer)rightFilter).next();
					}
					
					leftLine.write(l);
					rightLine.write(r);
					line.writeNext();
				}
			}
		};
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.playButton) {
			if (this.playThread == null) {
				this.playThread = playThread();
				this.playThread.start();
			}
		} else if (e.getSource() == this.leftFilterButton) {
			this.leftFilter = selectFilter();
		} else if (e.getSource() == this.rightFilterButton) {
			this.rightFilter = selectFilter();
		}
	}
	
	protected OutputLine selectFilter() {
		Mixer m = Mixer.getCurrentMixer();
		
		JComboBox box = new JComboBox(m.getLineNames());
		JOptionPane.showMessageDialog(null, box, "Select line", JOptionPane.PLAIN_MESSAGE);
		
		return m.getLine((String) box.getSelectedItem());
	}
}
