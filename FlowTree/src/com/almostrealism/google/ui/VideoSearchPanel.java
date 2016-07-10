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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;

import com.almostrealism.google.YouTubeConnection;
import com.google.gdata.util.ServiceException;

public class VideoSearchPanel extends JPanel implements KeyListener {
	private YouTubeConnection connection;
	
	private JTextField searchField;
	private JList resultsList;
	private VideoListModel videoListModel;
	
	public VideoSearchPanel(YouTubeConnection c) {
		super(new BorderLayout());
		
		this.connection = c;
		
		this.searchField = new JTextField(15);
		this.searchField.addKeyListener(this);
		
		this.videoListModel = new VideoListModel();
		this.resultsList = new JList(this.videoListModel);
		this.videoListModel.setVideos(new ArrayList());
		this.resultsList.setCellRenderer(this.videoListModel);
		
		super.add(this.searchField, BorderLayout.NORTH);
		super.add(new JScrollPane(this.resultsList), BorderLayout.CENTER);
	}

	protected void search() {
		this.search(this.searchField.getText());
	}
	
	public void search(String s) {
		try {
			this.videoListModel.setVideos(this.connection.searchVideos(s));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "IO error searching videos: " + e.getMessage(),
										"IO Error", JOptionPane.ERROR_MESSAGE);
		} catch (ServiceException e) {
			JOptionPane.showMessageDialog(this, "Service error searching videos: " + e.getMessage(),
										"Service Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void addListSelectionListener(ListSelectionListener listener) { this.resultsList.addListSelectionListener(listener); }
	
	public void keyPressed(KeyEvent e) { }

	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == e.VK_ENTER) this.search();
	}
	
	public static void main(String args[]) throws MalformedURLException {
		JFrame f = new JFrame("Rings: YouTube");
		f.getContentPane().add(new VideoSearchPanel(YouTubeConnection.getConnection()));
		f.setSize(200, 300);
		f.setVisible(true);
	}
}
