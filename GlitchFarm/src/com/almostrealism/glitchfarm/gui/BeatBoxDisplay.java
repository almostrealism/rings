package com.almostrealism.glitchfarm.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.almostrealism.glitchfarm.line.OutputLine;
import com.almostrealism.glitchfarm.mixer.Mixer;
import com.almostrealism.glitchfarm.obj.Sample;
import com.almostrealism.glitchfarm.util.DataProducer;
import com.almostrealism.glitchfarm.util.LineUtilities;

public class BeatBoxDisplay extends JPanel implements ActionListener, KeyListener, DataProducer {
	private int length = 16;
	
	private JPanel mainPanel;
	private JButton changeLineButton;
	
	private List samples, lines;
	private List buttons[];
	private ButtonGroup buttonGroups[];
	
	private Mixer mixer;
	private OutputLine mainLine;
	private boolean stop = true;
	
	private Sample currentSample;
	private int index;
	
	public BeatBoxDisplay(AudioFormat format) {
		super(new BorderLayout());
		
		this.samples = new ArrayList();
		this.lines = new ArrayList();
		this.buttons = new List[length];
		this.buttonGroups = new ButtonGroup[length];
		
		for (int i = 0; i < this.length; i++) {
			this.buttons[i] = new ArrayList();
			this.buttonGroups[i] = new ButtonGroup();
		}
		
		this.mainLine = LineUtilities.getLine(format);
		
		this.mainPanel = new JPanel(new GridBagLayout());
		
		this.changeLineButton = new JButton("Line...");
		this.changeLineButton.addActionListener(this);
		this.changeLineButton.addKeyListener(this);
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		statusPanel.add(this.changeLineButton);
		
		super.add(this.mainPanel, BorderLayout.CENTER);
		super.add(statusPanel, BorderLayout.SOUTH);
	}
	
	public void setMixer(Mixer mixer) { this.mixer = mixer; }
	
	public void addRow(Sample s) {
		if (!this.stop) return;
		
		this.samples.add(s);
		OutputLine l = LineUtilities.getLine(s.getFormat());
		this.lines.add(l);
		
		SampleDisplayPane p = new SampleDisplayPane(s, l);
		p.setScrollingDisplay(false);
		p.setDownsampleRate(SampleDisplayPane.ZOOM3);
		
		SampleControlsDisplay pc = new SampleControlsDisplay(s);
		pc.setSampleDisplayPane(p);
		
		JScrollPane sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
			public Dimension getMinimumSize() {
				return new Dimension(80, 40);
			}
			
			public Dimension getMaximumSize() {
				return new Dimension(120, 40);
			}
		};
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.gridy = this.samples.size();
		c.gridx++;
		c.gridwidth = 1;
		this.mainPanel.add(pc, c);
		
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridx++;
		c.gridwidth = 6;
		this.mainPanel.add(sp, c);
		
		c.gridwidth = 1;
		c.gridx += 6;
		
		for (int i = 0; i < this.length; i++) {
			BeatBoxRadioButton b = new BeatBoxRadioButton(s);
			b.addKeyListener(this);
			this.buttons[i].add(b);
			this.buttonGroups[i].add(b);
			
			c.gridx++;
			this.mainPanel.add(b, c);
		}
		
		this.validate();
		this.repaint();
	}
	
	public void setOutputLine(OutputLine line) {
		this.mainLine = line;
	}
	
	public void play() {
		if (this.stop) {
			this.stop = false;
			this.playThread().start();
		} else {
			this.stop = true;
		}
	}
	
	public Thread playThread() {
		return new Thread() {
			public void run() {
				i: for (int i = 0; !stop; i++) {
					if (i >= length) i = 0;
					
					Iterator itr = buttons[i].iterator();
					while (itr.hasNext()) {
						BeatBoxRadioButton b = (BeatBoxRadioButton) itr.next();
						if (b.isSelected()) {
							b.sample.play(mainLine);
							continue i;
						}
					}
				}
			}
		};
	}
	
	public byte[] next() {
		byte b[] = null;
		if (this.currentSample != null)
			b = this.currentSample.next();
		
		if (b == null) {
			i: for (; index < length; index++) {
				Iterator itr = buttons[index].iterator();
				
				while (itr.hasNext()) {
					BeatBoxRadioButton a = (BeatBoxRadioButton) itr.next();
					if (a.isSelected()) {
						this.currentSample = a.sample;
						break i;
					}
				}
				
				if (index == length - 1) index = -1;
			}
			
			index++;
			b = this.currentSample.next();
		}
		
		return b;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.changeLineButton) {
			if (this.mixer == null) return;
			
			JComboBox box = new JComboBox(this.mixer.getLineNames());
			JOptionPane.showMessageDialog(null, box, "Select line", JOptionPane.PLAIN_MESSAGE);
			
			this.mainLine = this.mixer.getLine((String) box.getSelectedItem());
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P) {
			this.play();
		} else if (e.getKeyCode() == KeyEvent.VK_M) {
			
		}
	}

	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
	
	public String toString() { return "Beat Box"; }
}
