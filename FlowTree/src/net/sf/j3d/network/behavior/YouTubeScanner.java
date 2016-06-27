package net.sf.j3d.network.behavior;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import com.google.gdata.util.ServiceException;
import com.roxstream.google.YouTubeConnection;
import com.roxstream.google.YouTubeVideo;

import net.sf.j3d.network.NetworkClient;
import net.sf.j3d.network.Resource;
import net.sf.j3d.network.Server;
import net.sf.j3d.network.ServerBehavior;
import net.sf.j3d.network.db.Client;

public class YouTubeScanner implements ServerBehavior {
	private YouTubeConnection tube;
	private String search = "guitar";
	
	public YouTubeScanner() throws MalformedURLException {
		tube = YouTubeConnection.getConnection();
	}
	
	@Override
	public void behave(Server s, PrintStream out) {
		out.println("YouTubeScanner(" + search + "," + hashCode() + "): Starting scan");
		
		Iterator itr;
		try {
			itr = tube.searchVideos(search).iterator();
		} catch (IOException e) {
			e.printStackTrace(out);
			return;
		} catch (ServiceException e) {
			e.printStackTrace(out);
			return;
		}
		
		while (itr.hasNext()) {
			YouTubeVideo video = (YouTubeVideo) itr.next();

			out.println("YouTubeScanner(" + search + "," + hashCode() +
						"): Downloading " + video);
			
			try {
				Resource r = s.loadResource(video.videoUrl);
			} catch (IOException e) {
				e.printStackTrace(out);
			}
			
			System.exit(0);
		}
	}
	
	public static void main(String args[]) throws MalformedURLException {
		NetworkClient.main(new String[0]);
		
		YouTubeScanner scanner = new YouTubeScanner();
		if (args.length > 0) scanner.search = args[0];
		
		scanner.behave(Client.getCurrentClient().getServer(), System.out);
	}
}
