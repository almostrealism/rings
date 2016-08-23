package com.almostrealism.glitchfarm.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.almostrealism.glitchfarm.obj.Sample;
import com.almostrealism.glitchfarm.util.LineUtilities;
import com.almostrealism.glitchfarm.util.SampleIOUtilities;

public class LoadSamplePanel extends JPanel implements ActionListener {
	private JButton loadButton;
	private SampleDisplayPane display;
	private ActionListener listener;
	
	public LoadSamplePanel() {
		this.loadButton = new JButton("Load");
		this.loadButton.addActionListener(this);
		this.add(this.loadButton);
	}
	
	/**
	 * Sets an action listener that will be notified when a sample
	 * has been loaded.
	 * 
	 * @param l  ActionListener to notify on load.
	 */
	public void setLoadActionListener(ActionListener l) {
		this.listener = l;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.loadButton) {
			JFileChooser chooser = new JFileChooser();
			chooser.showOpenDialog(this);
			
			try {
				FileInputStream in = new FileInputStream(chooser.getSelectedFile());
				BufferedInputStream bin = new BufferedInputStream(in);
				AudioInputStream ain = AudioSystem.getAudioInputStream(bin);
				AudioFormat format = ain.getFormat();
				
				int frameSize = format.getFrameSize();
				byte data[][] = new byte[(int) ain.getFrameLength()][frameSize];
				
				Sample s = new Sample(data, format);
				this.display = new SampleDisplayPane(s, LineUtilities.getLine(s.getFormat()), true);
				SampleLoadProgressBar bar = new SampleLoadProgressBar(s, ain, this.display);
				
				this.removeAll();
				this.setLayout(new BorderLayout());
				this.add(new JScrollPane(this.display), BorderLayout.CENTER);
				this.add(bar, BorderLayout.SOUTH);
				this.invalidate();
				this.repaint();
				
				bar.start();
				
				if (this.listener != null)
					this.listener.actionPerformed(new ActionEvent(this, 0, null));
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(),
											"Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			} catch (UnsupportedAudioFileException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(),
											"Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}
}
