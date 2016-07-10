/*
 * Copyright 2008 RoxStream Media
 * 
 * This code was inspired by YouTubeService.java
 * Copyrighted by Google Inc. in 2006
 * 
 * The notice for the original file is as follows:
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.almostrealism.raytracer.Settings;
import com.google.gdata.client.Service;
import com.google.gdata.client.media.MediaService;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.ParseSource;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.CommentFeed;
import com.google.gdata.data.youtube.ComplaintFeed;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.FriendFeed;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.gdata.data.youtube.RatingFeed;
import com.google.gdata.data.youtube.SubscriptionFeed;
import com.google.gdata.data.youtube.UserProfileFeed;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.xml.XmlWriter;

public class YouTubeConnection extends MediaService {
	private static final String SERVICE_NAME = "youtube";
	private static final String SERVICE_VERSION = "YouTube-Java/1.0";
	private static final String AUTH_URL = "https://www.google.com/youtube";

	public static YouTubeConnection getConnection() throws MalformedURLException {
		return new YouTubeConnection(GoogleSettings.RINGS_CLIENT_ID, GoogleSettings.RINGS_YT_API_KEY);
	}

	public YouTubeConnection(String app, String user) throws MalformedURLException {
		this(app, user, new URL(AUTH_URL));
	}

	private YouTubeConnection(String app, String user, URL url) {
		super(SERVICE_NAME, app, url.getProtocol(), url.getHost() + ":" + url.getPort() + url.getPath());
		getRequestFactory().setHeader("X-GData-Key", user != null ? "key=" + user : null);
		getRequestFactory().setHeader("X-GData-Client", app);

		ExtensionProfile profile = getExtensionProfile();
		profile.addDeclarations(new ComplaintFeed());
		profile.addDeclarations(new CommentFeed());
		profile.addDeclarations(new FriendFeed());
		profile.addDeclarations(new PlaylistFeed());
		profile.addDeclarations(new PlaylistLinkFeed());
		profile.addDeclarations(new RatingFeed());
		profile.addDeclarations(new SubscriptionFeed());
		profile.addDeclarations(new UserProfileFeed());
		profile.addDeclarations(new VideoFeed());
	}

	@Override
	public String getServiceVersion() { return SERVICE_VERSION + ' ' + super.getServiceVersion(); }

	protected <E extends BaseEntry<?>> FormUploadToken newToken(URL url, E entry) throws IOException, ServiceException {
		Service.GDataRequest request = createInsertRequest(url);
		XmlWriter xw = request.getRequestWriter();
		entry.generateAtom(xw, extProfile);
		xw.flush();

		request.execute();

		ParseSource source = request.getParseSource();

		try {
			return FormUploadToken.parse(source.getInputStream());
		} finally {
			closeSource(source);
		}
	}

	public List searchVideos(String search) throws IOException, ServiceException {
		YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);
		query.setIncludeRacy(true);
		query.setVideoQuery(search);

		VideoFeed feed = this.query(query, VideoFeed.class);
		List vids = new ArrayList();

		for (VideoEntry entry : feed.getEntries()) vids.add(new YouTubeVideo(this, entry));

		return vids;
	}
	
	public static void main(String args[]) throws IOException, ServiceException {
		YouTubeConnection c = getConnection();
		List v = c.searchVideos(args[0]);
		Iterator itr = v.iterator();
		while (itr.hasNext()) {
			YouTubeVideo video = (YouTubeVideo) itr.next();
			
			System.out.println("Title: " + video.title);
			System.out.println("Description: " + video.description);
			System.out.println();
		}
	}
}
