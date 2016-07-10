package com.almostrealism.google.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.almostrealism.flow.Resource;
import com.almostrealism.flow.db.Client;
import com.almostrealism.google.YouTubeVideo;

public class VideoDownloadPanel extends JPanel {
	protected static class VideoDownload implements Runnable {
		private YouTubeVideo video;
		private double progress = 0.0;
		
		private Runnable repaint;
		
		public VideoDownload(YouTubeVideo video) { this.video = video; }
		
		public void setRepaint(Runnable repaint) { this.repaint = repaint; }
		
		public void start() { new Thread(this).start(); }

		public void run() {
			Client c = Client.getCurrentClient();
			Resource r = null;
			
			try {
				r = c.getServer().loadResource(video.videoUrl);
				this.progress = 1.0;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("VideoDownload[" + this.toString() + "]: " + r);
			if (r == null) this.progress = -1.0;
			SwingUtilities.invokeLater(this.repaint);
		}
		
		public int hashCode() { return this.video.hashCode(); }
		
		public boolean equals(Object o) {
			if (o instanceof VideoDownload == false)
				return false;
			else
				return ((VideoDownload)o).video.equals(this.video);
		}
		
		public String toString() {
			if (this.progress >= 1.0) {
				return this.video.title + " \t DONE";
			} else if (this.progress < 0.0) {
				return this.video.title + " \t FAILED";
			} else {
				return this.video.title;
			}
		}
	}
	
	private class DownloadListModel implements ListModel {
		public void addListDataListener(ListDataListener l) { listeners.add(l); }
		public void removeListDataListener(ListDataListener l) { listeners.remove(l); }
		public Object getElementAt(int index) { return downloads.get(index); }
		public int getSize() { return downloads.size(); }
	}
	
	private String downloadDir = "downloads";
	
	private List downloads, listeners;
	
	private JList downloadList;
	
	public VideoDownloadPanel() { this("downloads"); }
	
	public VideoDownloadPanel(String downloadDir) {
		super(new BorderLayout());
		
		this.downloads = new ArrayList();
		this.listeners = new ArrayList();
		
		this.downloadList = new JList(new DownloadListModel());
		
		super.add(new JScrollPane(this.downloadList), BorderLayout.CENTER);
	}
	
	public void addDownload(YouTubeVideo v) {
		VideoDownload d = new VideoDownload(v);
		d.setRepaint(new Runnable() { public void run() { downloadList.repaint(); }});
		this.downloads.add(d);
		
		Iterator itr = this.listeners.iterator();
		
		while (itr.hasNext()) {
			((ListDataListener)itr.next()).contentsChanged(new ListDataEvent(
													this.downloadList,
													ListDataEvent.CONTENTS_CHANGED,
													0, this.downloads.size()));
		}
		
		System.out.println("VideoDownloadPanel: Starting download of " + v);
		d.start();
	}
}
