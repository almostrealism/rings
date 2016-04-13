package com.roxstream.google;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;

public class YouTubeVideo {
	protected YouTubeConnection service;
	
	public String title;
	public String description;
	public String iconUrl, relatedUrl, videoUrl, mediaUrl;
	public String videoId;
	
	public VideoEntry entry;
	public MediaSource dataSource;

	private ImageIcon icon;
	
	public YouTubeVideo(YouTubeConnection service, VideoEntry ent) {
		this.service = service;
		this.entry = ent;
		
		this.title = entry.getTitle().getPlainText();
		this.description = entry.getMediaGroup().getDescription().getPlainTextContent();
		
		MediaThumbnail thumb = entry.getMediaGroup().getThumbnails().get(0);
		this.iconUrl = thumb.getUrl();
		
		this.relatedUrl = entry.getRelatedVideosLink().getHref();
		this.videoUrl = entry.getId();
		this.videoId = this.videoUrl.substring(this.videoUrl.lastIndexOf("/") + 1);
		this.videoUrl = "http://www.youtube.com/v/" + this.videoId;
		this.mediaUrl = "http://youtube.com/get_video.php?video_id=" + videoId +
						"&t=OEgsToPDskKE77lszrGBEa7DwhvIifc-&hl=en";
		this.dataSource = entry.getMediaSource();
	}
	
	public InputStream openStream() throws IOException { return entry.getMediaSource().getInputStream(); }
	
	public void downloadVideo(OutputStream out) throws MalformedURLException, IOException {
		InputStream in = new URL(this.mediaUrl).openStream();
		
		w: while (in.available() > -1) {
			int b = in.read();
			if (b < 0) break w;
			out.write(b);
			if (b < 0) break w;
		}
		
		out.flush();
		out.close();
		in.close();
	}
	
	public int hashCode() { return this.videoId.hashCode(); }
	
	public boolean equals(Object o) {
		if (o instanceof YouTubeVideo == false)
			return false;
		else
			return ((YouTubeVideo)o).videoId.equals(this.videoId);
	}
	
	public String toString() { return this.title; }
	
	public ImageIcon getIcon() throws MalformedURLException {
		if (this.icon == null)
			this.icon = new ImageIcon(new URL(this.iconUrl));
		
		return this.icon;
	}
	
	public List getRelatedVideos() throws MalformedURLException, IOException, ServiceException {
		List l = new ArrayList();
		
		if (relatedUrl != null) {
			VideoFeed videoFeed = service.getFeed(new URL(relatedUrl), VideoFeed.class);
			for(VideoEntry entry : videoFeed.getEntries() ) l.add(new YouTubeVideo(this.service, entry));
		}
		
		return l;
	}
}
