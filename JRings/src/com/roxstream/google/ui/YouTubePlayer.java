package com.roxstream.google.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.almostrealism.flow.NetworkClient;
import com.google.gdata.util.ServiceException;
import com.roxstream.google.YouTubeConnection;
import com.roxstream.google.YouTubeVideo;

public class YouTubePlayer extends JFrame implements Runnable, ListSelectionListener {
	public static YouTubePlayer player;
	
	private YouTubeConnection tube;
	
	private VideoSearchPanel searchPanel;
	private VideoInfoPanel infoPanel;
	private RelatedVideosPanel relatedPanel;
	private VideoPlayerPanel playerPanel;
	private VideoDownloadPanel downloadPanel;
	
	private JSplitPane searchDataSplit, videoInfoSplit, videoRelatedSplit;
	
	public YouTubePlayer(YouTubeConnection tube) {
		super("Rings: You Tube");
		
		if ("running".equals(System.getProperty("rings.status"))) {
			System.setProperty("rings.status", "");
			System.out.println("YouTubePlayer: Looks to be another running rings instance.");
			System.out.println("YouTubePlayer: I must run in the same JVM as rings.");
			System.exit(1);
		}
		
		NetworkClient.main(new String[0]);
		
		System.setProperty("rings.status", "running");
		
		this.tube = tube;
		this.searchPanel = new VideoSearchPanel(this.tube);
		this.infoPanel = new VideoInfoPanel();
		this.relatedPanel = new RelatedVideosPanel();
		this.playerPanel = new VideoPlayerPanel();
		this.downloadPanel = new VideoDownloadPanel();
		
		this.playerPanel.setDownloadPanel(this.downloadPanel);
		
		this.searchPanel.addListSelectionListener(this);
		
		this.relatedPanel.setBorder(BorderFactory.createTitledBorder("Related Videos:"));
		
		this.videoRelatedSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.playerPanel, this.relatedPanel);
		this.videoInfoSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.videoRelatedSplit, this.infoPanel);
		this.searchDataSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.searchPanel, this.videoInfoSplit);
		
		this.videoRelatedSplit.setDividerLocation(450);
		this.videoInfoSplit.setDividerLocation(400);
		this.searchDataSplit.setDividerLocation(300);
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Browse", this.searchDataSplit);
		tabs.addTab("Downloads", this.downloadPanel);
		
		super.getContentPane().add(tabs, BorderLayout.CENTER);
		
		super.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(YouTubePlayer.this).start();
			}
		});
	}
	
	public void setSelectedVideo(YouTubeVideo video) throws MalformedURLException, IOException, ServiceException {
		this.infoPanel.setVideo(video);
		this.relatedPanel.setVideo(video);
		this.playerPanel.setVideo(video);
	}
	
	public static void main(String args[]) throws MalformedURLException {
		player = new YouTubePlayer(YouTubeConnection.getConnection());
		player.setSize(1000, 650);
		player.setVisible(true);
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			this.setSelectedVideo((YouTubeVideo) ((JList)e.getSource()).getSelectedValue());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			if (!"running".equals(System.getProperty("rings.status"))) {
				this.player.setVisible(true);
				return;
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) { }
		}
	}
}
