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

package org.almostrealism.swing.displays;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * A ProgressDisplay object can be used to detect and display internal progress.
 * 
 * @author Mike Murray
 */
public class ProgressDisplay extends JPanel implements org.almostrealism.swing.ProgressMonitor {
  private final int incrementSize;
	private final int totalSize;
	private int increment;
  private boolean removeOnCompletion;
  
  private long startTime;
  private Timer timer;
  
  private final JProgressBar progressBar;
  private JLabel timeLabel;
	private final JLabel incLabel;

	/**
	 * Constructs a new ProgressDisplay object using the specified increment size
	 * and total size.
	 */
	public ProgressDisplay(int incrementSize, int totalSize) {
		this(incrementSize, totalSize, true);
	}
	
	/**
	 * Constructs a new ProgressDisplay object using the specified increment size
	 * and total size.
	 */
	public ProgressDisplay(int incrementSize, int totalSize, boolean showTime) {
		super(new GridLayout(0, 1));
		
		this.incrementSize = incrementSize;
		this.totalSize = totalSize;
		
		this.progressBar = new JProgressBar(0, totalSize / incrementSize);
		this.progressBar.setStringPainted(true);
		
		if (showTime) this.timeLabel = new JLabel("");
		this.incLabel = new JLabel("");
		
		this.reset();
		
		JPanel progressPanel = new JPanel(new FlowLayout());
		progressPanel.add(this.progressBar);
		super.add(progressPanel);
		
		if (showTime) {
			JPanel timePanel = new JPanel(new FlowLayout());
			timePanel.add(this.timeLabel);
			super.add(timePanel);
		}
		
		JPanel incPanel = new JPanel(new FlowLayout());
		incPanel.add(this.incLabel);
		super.add(incPanel);
	}
	
	public void setProgressBarColor(Color c) { this.progressBar.setForeground(c); }
	
	/**
	 * When set to true, this ProgressDisplay object will remove it self from it's parent
	 * when the monitered task is completed.
	 */
	public void setRemoveOnCompletion(boolean remove) { this.removeOnCompletion = remove; }
	
	/**
	 * Returns true if this ProgressDisplay object will remove it self from it's parent
	 * when the monitered task is completed.
	 */
	public boolean getRemoveOnCompletion() { return this.removeOnCompletion; }
	
	/**
	 * Returns the increment size used by this ProgressDisplay object.
	 */
	public int getIncrementSize() { return this.incrementSize; }
	
	/**
	 * Returns the total size of this ProgressDisplay object.
	 */
	public int getTotalSize() { return this.totalSize; }
	
	/**
	 * Returns the increment of this ProgressDisplay object.
	 */
	public int getIncrement() { return this.increment * this.incrementSize; }
	
	/**
	 * Increments this ProgressDisplay object.
	 */
	public void increment() {
		if (this.increment <= 0 && this.timer != null) {
			this.startTime = (new Date()).getTime();
			this.timer.start();
		}
		
		this.increment++;
		this.progressBar.setValue(this.increment);
		this.incLabel.setText(this.increment * this.incrementSize + " of " + this.totalSize);
		
		if (this.isComplete() && this.timer != null) {
			this.timer.stop();
		}
		
		if ((this.isComplete() && this.removeOnCompletion) && this.getParent() != null) {
			this.getParent().remove(this);
		}
	}
	
	/**
	 * Returns true if the task being monitored is complete, false otherwise.
	 */
	public boolean isComplete() {
		return this.increment * this.incrementSize >= this.totalSize;
	}
	
	public void reset() {
		this.incLabel.setText("");
		
		this.increment = 0;
		this.progressBar.setValue(0);
		
		if (this.timeLabel == null) return;
		
		ActionListener timeUpdater = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int time = (int)((new Date()).getTime() - startTime);
				
				int seconds = time / 1000;
				int minutes = seconds / 60;
				int hours = minutes / 60;
				
				seconds = seconds % 60;
				minutes = minutes % 60;
				
				if (seconds < 10)
					timeLabel.setText(hours + ":" + minutes + ":0" + seconds);
				else
					timeLabel.setText(hours + ":" + minutes + ":" + seconds);
			}
		};
		
		this.timer = new Timer(500, timeUpdater);
		this.timer.setRepeats(true);
		this.timeLabel.setText("");
	}
}
