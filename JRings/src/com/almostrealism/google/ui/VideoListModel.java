package com.almostrealism.google.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.almostrealism.google.YouTubeVideo;

public class VideoListModel implements ListModel, ListCellRenderer {
	private List listeners, videos;
	
	public VideoListModel() {
		this.listeners = new ArrayList();
	}
	
	public void setVideos(List videos) {
		this.videos = videos;
		Iterator itr = this.listeners.iterator();
		
		while (itr.hasNext()) {
			ListDataListener l = (ListDataListener) itr.next();
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.videos.size() - 1));
		}
	}
	
	public void addListDataListener(ListDataListener e) { this.listeners.add(e); }
	
	public void removeListDataListener(ListDataListener l) { this.listeners.remove(l); }
	
	public Object getElementAt(int index) {
		if (this.videos == null)
			return null;
		else
			return this.videos.get(index);
	}

	public int getSize() {
		if (this.videos == null)
			return 0;
		else
			return this.videos.size();
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		YouTubeVideo v = (YouTubeVideo) value;
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		if (isSelected) {
			p.setOpaque(true);
			p.setBackground(Color.blue);
		}
		
		try {
			p.add(new JLabel(v.getIcon()), BorderLayout.WEST);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		JTextArea l = new JTextArea(v.title + "\n" + v.description, 3, 20);
		l.setPreferredSize(new Dimension(200, 40));
		l.setEditable(false);
		
		p.add(l, BorderLayout.CENTER);
		
		return p;
	}
}
