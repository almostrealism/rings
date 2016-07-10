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

package com.almostrealism.google.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.almostrealism.google.YouTubeVideo;

public class VideoInfoPanel extends JPanel {
	private JTextArea textArea;
	
	public VideoInfoPanel() {
		super(new BorderLayout());
		
		this.textArea = new JTextArea("No video selected");
		this.textArea.setEditable(false);
		
		super.add(new JScrollPane(this.textArea), BorderLayout.CENTER);
	}
	
	public void setVideo(YouTubeVideo video) {
		this.textArea.setText(video.description);
	}
}
