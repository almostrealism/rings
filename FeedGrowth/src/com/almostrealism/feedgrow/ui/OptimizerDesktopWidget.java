/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
