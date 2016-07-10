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

package com.almostrealism.flow.behavior;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import com.almostrealism.flow.NetworkClient;
import com.almostrealism.flow.Resource;
import com.almostrealism.flow.Server;
import com.almostrealism.flow.ServerBehavior;
import com.almostrealism.flow.db.Client;
import com.almostrealism.google.YouTubeConnection;
import com.almostrealism.google.YouTubeVideo;
import com.google.gdata.util.ServiceException;

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
