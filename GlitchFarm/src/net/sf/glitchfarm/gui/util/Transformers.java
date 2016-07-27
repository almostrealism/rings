package net.sf.glitchfarm.gui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import net.sf.glitchfarm.obj.Sample;
import net.sf.glitchfarm.transform.BinaryTransformer;
import net.sf.glitchfarm.transform.LinearResampleTransformer;
import net.sf.glitchfarm.transform.SampleTransformer;
import net.sf.glitchfarm.transform.gui.BinaryTransformerEditor;
import net.sf.glitchfarm.transform.gui.LinearResampleTransformerEditor;
import net.sf.glitchfarm.transform.gui.TransformerTabbedPane;

public class Transformers {
	public static List<SampleTransformer> available = new ArrayList();
	
	/**
	 * Initializes the list of available transformers.
	 */
	public static void init() {
		available.add(new BinaryTransformer());
		available.add(new LinearResampleTransformer());
	}
	
	/**
	 * Creates an editor for the specified transformer.
	 */
	public static Component createEditor(SampleTransformer t) {
		if (t instanceof BinaryTransformer)
			return new BinaryTransformerEditor((BinaryTransformer) t);
		else if (t instanceof LinearResampleTransformer)
			return new LinearResampleTransformerEditor((LinearResampleTransformer) t);
		else
			return null;
	}
	
	/**
	 * Displays a TransformerTabbedPane that allows the user to transform
	 * the specified sample.
	 * 
	 * @param s  Sample to transform.
	 */
	public static void showTransformPanel(Sample s) {
		TransformerTabbedPane t = new TransformerTabbedPane(s);
		
		final JFrame f = new JFrame("Transform Sample");
		f.getContentPane().add(t);
		f.setSize(300, 500);
		
		t.setDoneListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.dispose();
			}
		});
		
		f.setVisible(true);
	}
}
