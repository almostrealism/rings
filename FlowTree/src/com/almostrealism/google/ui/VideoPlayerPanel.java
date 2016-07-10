package com.almostrealism.google.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.InputSourceStream;
import javax.media.protocol.PullSourceStream;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jflash.FlashPane;

import com.almostrealism.google.GoogleSettings;
import com.almostrealism.google.YouTubeVideo;
import com.jimischopp.jyoutube.JYouTube;
import com.jpackages.jflashplayer.FlashPanel;
import com.jpackages.jflashplayer.JFlashInvalidFlashException;
import com.jpackages.jflashplayer.JFlashLibraryLoadFailedException;

public class VideoPlayerPanel extends JPanel implements ActionListener {
	private YouTubeVideo video;
	private Player mediaPlayer;
	private Component videoc;
	
	private VideoDownloadPanel downloadPanel;
	
	public VideoPlayerPanel() {
		super(new BorderLayout());
		JButton download = new JButton("Download");
		JButton rtpButton = new JButton("Stream via RTP...");
		download.addActionListener(this);
		rtpButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(download);
		buttonPanel.add(rtpButton);
		
		super.add(buttonPanel, BorderLayout.SOUTH);
		
		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
	}
	
	public void setDownloadPanel(VideoDownloadPanel downloadPanel) { this.downloadPanel = downloadPanel; }
	
	public void setVideo(YouTubeVideo video) throws MalformedURLException, IOException {
		this.video = video;
		
		try {
			if (this.videoc != null) super.remove(this.videoc);
			
//			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(video.videoUrl).openStream(), "US-ASCII"));
//			
//			w: while (true) {
//				String line = in.readLine();
//				if (line == null)
//					break w;
//				else
//					System.out.println(line);
//			}
			
			DataSource d = new DataSource() {
				public PullSourceStream[] getStreams() {

					PullSourceStream [] streams = new PullSourceStream [1];
					InputSourceStream iss = null;
					try {
						iss = new InputSourceStream(VideoPlayerPanel.this.video.openStream(),
																		new ContentDescriptor(ContentDescriptor.RAW));
						System.out.println("VideoPlayerPanel DataSource: Opened " + iss);
					} catch (IOException e) {
						showError(e.getMessage());
					}
					streams[0] = iss;
					return streams;
				}

				public void connect() throws IOException {
				}

				public void disconnect() {
				}

				public String getContentType() {
					return "swf";
				}
				
				public Object getControl(String arg0) {
					return null;
				}

				public Object[] getControls() {
					return null;
				}

				public Time getDuration() {
					return null;
				}

				public void start() throws IOException {}

				public void stop() throws IOException {}
			};
			
			this.mediaPlayer = Manager.createRealizedPlayer(d);
			this.videoc = mediaPlayer.getVisualComponent();
			Component controls = mediaPlayer.getControlPanelComponent();
			
			super.add(videoc, BorderLayout.CENTER);
			super.repaint();
			
			mediaPlayer.start();
		} catch (NoPlayerException npe) {
			showError("No media player found");
		} catch (CannotRealizeException cre) {
			showError("Could not realize media player");
		} catch (IOException ioe) {
			showError("Error reading from the source");
		}
	}
	
	public void showError(String error) {
		JOptionPane.showMessageDialog(this, error, "Video Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void actionPerformed(ActionEvent e) { this.downloadPanel.addDownload(video); }
}
