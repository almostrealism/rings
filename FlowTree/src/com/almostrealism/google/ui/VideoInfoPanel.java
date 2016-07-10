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
