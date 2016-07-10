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

package com.almostrealism.flow.interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.text.DateFormatter;

import com.almostrealism.raytracer.Settings;

public class RTSPInterface implements Runnable {
	private int port = 554;
	private ServerSocket server;
	private boolean die;
	
	public RTSPInterface() throws IOException { this(new Properties()); }
	
	public RTSPInterface(Properties p) throws IOException {
		this.port = Integer.parseInt(p.getProperty("rtsp.port", "554"));
		this.server = new ServerSocket(this.port);
		if (this.port == 0)
			System.out.println("RTSP: port = " + this.server.getLocalPort());
	}
	
	public void run() {
		while (!die) {
			try {
				Socket s = this.server.accept();
				System.out.println("RTSP: Accepted " + s);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				
				String file = "no file";
				String adgent = "an unknown adgent";
				String bandwidth = "unspecified";
				String respondTo = null;
				String code = null;
				
				w: while (true) {
					String line = in.readLine();
					
					if (line == null) {
						System.out.println("RTSP: End of stream.");
						break w;
					}
					
					if (line.length() <= 0) {
						if (respondTo == null) {
							System.out.println("RTSP: Nothing to respond to.");
							continue w;
						}
						

						if (respondTo.equals("DESCRIBE")) {
//							String contentBase = file.substring(0, file.indexOf("/", file.indexOf("//") + 2));
							String contentBase = file;
//							file = file.substring(contentBase.length());
							int contentLength = 0; // TODO calculate content length
							
							StringBuffer describe = new StringBuffer();
							describe.append("v=0\n");
							describe.append("o=StreamingServer 3414243015 1157573193000 IN IP4 160.39.80.23\n");
							describe.append("s=" + file.indexOf("/", file.indexOf("//") + 2) + "\n");
							describe.append("u=http:///\n");
							describe.append("e=root@\n");
							describe.append("c=IN IP4 0.0.0.0\n");
							describe.append("b=AS:14\n");
							describe.append("t=0 0\n");
							describe.append("a=control:*\n");
							describe.append("a=maxprate:1.000000\n");
							describe.append("a=range:npt=0-   2.00000\n");
							describe.append("m=video 0 RTP/AVP 96\n");
							describe.append("b=AS:14\n");
							describe.append("b=TIAS:14\n");
							describe.append("a=maxprate:0\n");
							describe.append("a=rtpmap:96 X-QT/600\n");
							describe.append("a=control:trackID=3\n");
							
							System.out.println("RTSP: Sending " + file + " description to " + adgent + " at " + s.getRemoteSocketAddress());
							System.out.println("RTSP: Bandwidth is " + bandwidth);
							
							contentLength = describe.length();
							
							DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz");
							String date = dateFormat.format(new Date());
							
							out.write("RTSP/1.0 200" + (code == null ? "" : " " + code) + " OK\n");
							out.write("Server: RINGS/" + Settings.version + "\n");
							out.write("Cseq: 1\n");
							out.write("Cache-Control: must-revalidate\n");
							out.write("Date: " + date);
							out.write("Content-Type: application/sdp\n");
							out.write("Content-length: " + contentLength + "\n");
							System.out.println("Content-length: " + contentLength);
							out.write("x-Accept-Retransmit: our-retransmit\n");
							out.write("x-Accept-Dynamic-Rate: 1\n");
							out.write("Content-Base: " + contentBase + "/\n");
//							out.write("Session: 06e1a96cd3b6a4bfeead313cb273162d;timeout=60\n");
							out.write("\n");
							out.write(describe.toString());
							System.out.println(describe.toString());
							out.flush();
						} else {
							System.out.println("RTSP: No response for " + respondTo);
						}
						
						continue w;
					}
					
					if (line.startsWith("PLAY")) {
						System.out.println("RTSP: Play " + line.substring(5));
					} else if (line.startsWith("Accept: ")) {
						System.out.println("RTSP: " + adgent + " accepts " + line.substring(8));
					} else if (line.startsWith("DESCRIBE")) {
						file = line.split(" ")[1];
						respondTo = "DESCRIBE";
						System.out.println("RTSP: DESCRIBE");
					} else if (line.startsWith("User-Agent:")) {
						adgent = line.substring(12);
					} else if (line.startsWith("Bandwidth: ")) {
						bandwidth = line.substring(11);
					} else {
						System.out.println("RTSP: " + line);
					}
				}
				
				System.out.println("RTSP: Sending " + file + " to " + adgent.split(" ")[0] + " at " + s.getRemoteSocketAddress());
				System.out.println("RTSP: Done");
				in.close();
				out.close();
				s.close();
			} catch (IOException ioe) {
				System.out.println("RTSP: " + ioe.getMessage());
			}
		}
	}
	
	public void die() { this.die = true; }
	
	public static void main(String args[]) throws IOException {
		Properties p = new Properties();
		
		if (args.length > 0)
			p.load(new FileInputStream(args[0]));
		else
			p.load(new FileInputStream("node.conf"));
		
		Thread t = new Thread(new RTSPInterface(p));
		t.start();
	}
}
