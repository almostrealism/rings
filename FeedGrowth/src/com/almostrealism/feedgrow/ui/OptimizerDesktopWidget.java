package com.almostrealism.feedgrow.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.almostrealism.feedgrow.optimization.SimpleOrganOptimizer;
import com.almostrealism.ui.ScrollingTextDisplay;

public class OptimizerDesktopWidget<T> extends JPanel {
	private SimpleOrganOptimizer<T> optimizer;
	private ScrollingTextDisplay display;
	
	public OptimizerDesktopWidget(SimpleOrganOptimizer<T> opt) {
		super(new BorderLayout());
		
		this.optimizer = opt;
		
		ScrollingTextDisplay.TextProducer producer = new ScrollingTextDisplay.TextProducer() {
			public String nextPhrase() {
				return optimizer.getConsole().lastLine();
			}
		};
		
		this.display = new ScrollingTextDisplay(producer, 30);
		this.add(display, BorderLayout.CENTER);
	}
	
	public static void main(String args[]) {
		
	}
}
