package net.sf.glitchfarm.gui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.sf.glitchfarm.gui.BeatBoxDisplay;
import net.sf.glitchfarm.gui.SampleDisplayPane;
import net.sf.glitchfarm.obj.Sample;
import net.sf.glitchfarm.transform.LinearResampleTransformer;
import net.sf.glitchfarm.transform.ResampleTransformer;

public class MixerMenus {
	public static Hashtable folders;
	
	public static JPopupMenu addSamplePopupMenu;
	public static Sample currentSample;
	public static SampleDisplayPane currentDisplayPane;
	
	protected static ResampleTransformer bpmChanger;
	
	public static void init() {
		folders = new Hashtable();
		addSamplePopupMenu = new JPopupMenu();
		
		addActionFolder("Set BPM");
		addAction("Set BPM", new SetBPMAction(1));
		addAction("Set BPM", new SetBPMAction(2));
		addAction("Set BPM", new SetBPMAction(4));
		addAction("Set BPM", new SetBPMAction(8));
		addAction("Set BPM", new SetBPMAction(16));
		
		bpmChanger = new LinearResampleTransformer();
		
		JMenuItem changeBpmItem = new JMenuItem("Change BPM");
		changeBpmItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(null, "New BPM", "Change BPM", JOptionPane.INFORMATION_MESSAGE);
				if (s == null) return;
				
				double newBpm = Double.parseDouble(s);
				
				bpmChanger.setRatio(currentSample.getBPM() / newBpm);
				bpmChanger.transform(currentSample);
				
				if (currentDisplayPane != null) {
					currentDisplayPane.updateControlInfo();
					currentDisplayPane.repaint();
				}
			}
		});
		
		addSamplePopupMenu.add(changeBpmItem);
	}
	
	public static void addBeatBox(String name, final BeatBoxDisplay b) {
		JMenuItem addSampleToBoxMenuItem = new JMenuItem("Add to " + name);
		addSampleToBoxMenuItem.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Sample s = currentSample.shallowCopy();
				s.unloop();
				b.addRow(s);
			}
		});
		
		addSamplePopupMenu.add(addSampleToBoxMenuItem);
	}
	
	public static void addActionFolder(String name) {
		JMenu menu = new JMenu(name);
		addSamplePopupMenu.add(menu);
		folders.put(name, menu);
	}
	
	public static void addAction(Action a) {
		addSamplePopupMenu.add(a);
	}
	
	public static void addAction(String folder, Action a) {
		((JMenu)folders.get(folder)).add(a);
	}
	
	public static void showAddMenu(Component c, int x, int y) {
		addSamplePopupMenu.show(c, x, y);
	}
}
