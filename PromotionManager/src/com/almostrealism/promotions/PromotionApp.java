package com.almostrealism.promotions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.almostrealism.promotions.persist.PromotionDatabase;
import com.almostrealism.promotions.ui.CampaignPanel;

/**
 * Provides the main method which will bring up a window with a
 * {@link CampaignPanel}, menu bar, etc.
 * 
 * @author  Michael Murray
 */
public class PromotionApp {
	public static void main(String args[]) throws FileNotFoundException {
		String file = "PromotionDatabase.xml";
		if (args.length > 0) file = args[0];
		
		Object loaded;
		
		if (file.equals("-new")) {
			loaded = new PromotionDatabase();
		} else {
			try (XMLDecoder d = new XMLDecoder(new FileInputStream(file))) {
				loaded = d.readObject();
			}
		}
		
		final PromotionDatabase db = (PromotionDatabase) loaded;
		
		CampaignPanel cp = new CampaignPanel(db);
		
		final JFrame frame = new JFrame("Almost Realism Promotions System");
		
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser();
				if (f.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				
				try (XMLEncoder enc = new XMLEncoder(new FileOutputStream(f.getSelectedFile()))) {
					enc.writeObject(db);
				} catch (FileNotFoundException fnf) {
					fnf.printStackTrace();
				}
			}
		});
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(saveAsItem);
		
		JMenu materialMenu = new JMenu("Material");
		materialMenu.add(cp.getMaterialsPanel().getAddMaterialAction());
		
		JMenu targetsMenu = new JMenu("Targets");
		targetsMenu.add(cp.getTargetsPanel().getAddTargetAction());
		targetsMenu.add(cp.getTargetsPanel().getEngageTargetAction());
		
		JMenu servicesMenu = new JMenu("Services");
		servicesMenu.add(db.getAddEmailServiceAction());
		
		JMenuBar bar = new JMenuBar();
		bar.add(fileMenu);
		bar.add(materialMenu);
		bar.add(targetsMenu);
		bar.add(servicesMenu);
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(cp, BorderLayout.CENTER);
		frame.getContentPane().add(bar, BorderLayout.NORTH);
		
		frame.setSize(600, 400);
		frame.setLocation(300, 200);
		frame.setVisible(true);
	}
}
