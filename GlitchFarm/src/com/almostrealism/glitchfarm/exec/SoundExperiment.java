package com.almostrealism.glitchfarm.exec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.almostrealism.glitchfarm.gui.LoadSamplePanel;
import com.almostrealism.glitchfarm.gui.OutputLineTabbedPane;
import com.almostrealism.glitchfarm.gui.util.MixerMenus;
import com.almostrealism.glitchfarm.gui.util.Transformers;
import com.almostrealism.glitchfarm.gui.views.TabbedMixView;
import com.almostrealism.glitchfarm.mixer.Mixer;
import com.almostrealism.glitchfarm.util.LineUtilities;

public class SoundExperiment {
	private static ActionListener loadListener;
	
	public static void main(String args[]) throws UnsupportedAudioFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		try {
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find the TinyLookAndFeel.");
		}
		
		InputStream in = SoundExperiment.class.getResourceAsStream("WavConf.wav");
		LineUtilities.initDefaultAudioFormat(in);
		
		MixerMenus.init();
		Mixer mixer = new Mixer();
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
		JFrame frame = new JFrame("Glitch Farm");
		frame.setSize(800, 600);
		frame.add(tabs);
		
		final JTabbedPane loadTabs = new JTabbedPane();
		
		loadListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoadSamplePanel p = new LoadSamplePanel();
				p.setLoadActionListener(loadListener);
				loadTabs.addTab("", p);
			}
		};
		
		LoadSamplePanel loadPanel = new LoadSamplePanel();
		loadPanel.setLoadActionListener(loadListener);
		loadTabs.addTab("", loadPanel);
		
		loadListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoadSamplePanel p = new LoadSamplePanel();
				p.setLoadActionListener(loadListener);
				loadTabs.addTab("", p);
			}
		};
		
		tabs.addTab("Load Sample", loadTabs);
		
		final JTabbedPane splits = new JTabbedPane();
		tabs.addTab("Pieces", splits);
		
		MixerMenus.addActionFolder("Split");
		MixerMenus.addAction("Split", new SplitAction("1", 1, splits));
		MixerMenus.addAction("Split", new SplitAction("2", 2, splits));
		MixerMenus.addAction("Split", new SplitAction("4", 4, splits));
		MixerMenus.addAction("Split", new SplitAction("8", 8, splits));
		MixerMenus.addAction("Split", new SplitAction("16", 16, splits));
		
		Transformers.init();
		
		MixerMenus.addAction(new AbstractAction("Transform") {
			public void actionPerformed(ActionEvent e) {
				Transformers.showTransformPanel(MixerMenus.currentSample);
			}
		});
		
//		JFrame keyboardSamplesFrame = new JFrame("Key Board Samples");
//		keyboardSamplesFrame.getContentPane().add(kd);
//		keyboardSamplesFrame.addKeyListener(kd);
//		keyboardSamplesFrame.setSize(500, 160);
//		keyboardSamplesFrame.setLocation(0, 300);
//		keyboardSamplesFrame.setVisible(true);
		
		TabbedMixView mixView = new TabbedMixView();
		tabs.addTab("Mixer", mixView);
		
		MixerMenus.addBeatBox("Left", mixView.getCurrentLeftBeatBox());
		MixerMenus.addBeatBox("Right", mixView.getCurrentRightBeatBox());
		
		OutputLineTabbedPane olp = new OutputLineTabbedPane(mixer);
		tabs.addTab("Lines and Filters", olp);
		
		frame.setVisible(true);
	}
}
