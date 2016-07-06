/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.resources;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.util.ImagingException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.almostrealism.flow.Resource;
import com.almostrealism.flow.Server;
import com.almostrealism.flow.Server.IOStreams;
import com.almostrealism.flow.db.Client;
import com.almostrealism.io.ScpDownloader;
import com.sun.media.jai.codec.FileCacheSeekableStream;

public class ImageResource implements Resource {
	private String uri;
	
	private int data[];
	private int x, y, w, h;
	
	public ImageResource() {}
	
	public ImageResource(String uri, int data[]) {
		this.uri = uri;
		this.data = data;
		
		if (this.data != null) {
			this.w = this.data[0];
			this.h = this.data[1];
		}
	}
	
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	public void setWidth(int w) { this.w = w; }
	public void setHeight(int h) { this.h = h; }
	
	public int getX() { return this.x; }
	public int getY() { return this.y; }
	public int getWidth() { return this.w; }
	public int getHeight() { return this.h; }
	
	public void load(Server.IOStreams io) throws IOException {
		io.out.writeInt(this.x);
		io.out.writeInt(this.y);
		io.out.writeInt(this.w);
		io.out.writeInt(this.h);
		
		int w = io.in.readInt();
		int h = io.in.readInt();
		this.data = new int[2 + w * h];
		this.data[0] = w;
		this.data[1] = h;
		
		System.out.println("ImageResource: Reading " + w + "x" + h + " image...");
		
		int one = data.length / 4;
		int two = data.length / 2;
		int three = 3 * one;
		int four = data.length - 1;
		
		for (int i = 2; i < data.length; i++) {
			this.data[i] = io.in.readInt();
			
			if (i == one)
				System.out.println("ImageResource: Load 25% Complete.");
			else if (i == two)
				System.out.println("ImageResource: Load 50% Complete.");
			else if (i == three)
				System.out.println("ImageResource: Load 75% Complete.");
			else if (i == four)
				System.out.println("ImageResource: Load 100% Complete.");
		}
	}
	
	public void loadFromURI() {
		t: try {
			RenderedImage im = null;
			
			if (uri.startsWith("scp://")) {
				String ur = uri.substring(6);
				int index = ur.indexOf("|");
				String host = ur.substring(0, index);
				ur = ur.substring(index + 1);
				index = ur.indexOf("|");
				String user = ur.substring(0, index);
				ur = ur.substring(index + 1);
				index = ur.indexOf("/");
				String passwd = ur.substring(0, index);
				ur = ur.substring(index);
				
				final String fur = ur;
				
				System.out.println("ImageResource: Loading image from " + user + "@" + host + ur);
				
				PipedInputStream in = new PipedInputStream();
				final PipedOutputStream out = new PipedOutputStream(in);
				FileCacheSeekableStream seekableInput = new FileCacheSeekableStream(in);
				
				final ScpDownloader scpd = ScpDownloader.getDownloader(host, user, passwd);
				
				ThreadGroup g = null;
				Client c = Client.getCurrentClient();
				if (c != null) g = c.getServer().getThreadGroup();
				Thread dt = new Thread(g, "ImageResource SCP Loader") {
					public void run() {
						try {
							scpd.download(fur, out);
						} catch (IOException ioe) {
							System.out.println("Server: " + ioe.getMessage());
						}
					}
				};
				dt.start();
				im = JAI.create("stream", seekableInput);
			} else if (uri.startsWith("resource://")) {
				IOStreams io = Client.getCurrentClient().getServer().parseResourceUri(uri);
				
				if (io == null) {
					System.out.println("ImageResource: Resource unavailable.");
					return;
				}
				
				this.load(io);
				io.close();
			} else {
				System.out.println("ImageResource: Loading image from " + uri);
				im = JAI.create("url", new URL(uri));
			}
			
			if (im == null && this.data == null) throw new IOException();
			
			if (im != null) {
				int w = im.getWidth();
				int h = im.getHeight();
				
				System.out.println("ImageResource: Buffer is " + w + " x " + h);
				
				BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				bim.setData(im.copyData(null));
				
				int index = 2;
				this.data = new int[2 + w * h];
				this.data[0] = w;
				this.data[1] = h;
				
				System.out.println("ImageResource: Converting...");
				
				for(int j = 0; j < h; j++) {
					for(int i = 0; i < w; i++) {
						this.data[index++] = bim.getRGB(i, j);
					}
				};
				
				System.out.println("ImageResource: Converted.");
			}
			
			System.out.println("ImageResource: Loaded " + this.data[0] + " x " + this.data[1]);
		} catch (ImagingException ie) {
			System.out.println("Server: Imaging exception caused by " + ie.getRootCause());
		} catch (com.sun.media.jai.codecimpl.util.ImagingException cie) {
			System.out.println("Server: Imaging exception caused by " + cie.getRootCause());
		} catch (IndexOutOfBoundsException oob) {
			System.out.println("Server: Index out of bounds while loading image.");
			oob.printStackTrace(System.out);
		} catch (NullPointerException np) {
			System.out.println("Server: Error loading image (null pointer).");
			np.printStackTrace(System.out);
		} catch (Exception e) {
			System.out.println("Server: Error loading image - " + e);
		}
	}
	
	public void send(Server.IOStreams io) throws IOException {
		if (this.data == null) return;
		
		int sx = this.x;
		int sy = this.y;
		int sw = this.w;
		int sh = this.h;
		
		int rgb[] = this.data;
		
		sx = io.in.readInt();
		sy = io.in.readInt();
		sw = io.in.readInt();
		sh = io.in.readInt();
		
		if (sw == 0) sw = this.w;
		if (sh == 0) sh = this.h;
		
		rgb = this.clip(sx, sy, sw, sh);
		
		System.out.println("ImageResource: Sending " + rgb[0] + "x" + rgb[1] + " image...");
		
		io.out.writeInt(rgb[0]);
		io.out.writeInt(rgb[1]);
		
		int one = rgb.length / 4;
		int two = rgb.length / 2;
		int three = 3 * one;
		int four = rgb.length - 1;
		
		for (int i = 2; i < rgb.length; i++) {
			io.out.writeInt(rgb[i]);
			
			if (i == 2)
				System.out.println("ImageResource: Sent first pixel.");
			else if (i == one)
				System.out.println("ImageResource: Send 25% Complete.");
			else if (i == two)
				System.out.println("ImageResource: Send 50% Complete.");
			else if (i == three)
				System.out.println("ImageResource: Send 75% Complete.");
			else if (i == four)
				System.out.println("ImageResource: Send 100% Complete.");
		}
		
		System.out.println("ImageResource: Flushing output stream.");
		io.out.flush();
		System.out.println("ImageResource: Done.");
	}
	
	
	// TODO This is probably broken since cx is not used.
	public int[] clip(int cx, int cy, int cw, int ch) {
		System.out.println("ImageResource: Clipping to "
							+ cx + ", " + cy + ", " +
							cw + ", " + ch + "...");
		
		if (cx == 0 && cy == 0 && cw == this.w && ch == this.h) {
			int out[] = new int[this.data.length];
			System.arraycopy(this.data, 0, out, 0, this.data.length);
			return out;
		}
		
//		cx = cx - this.x;
		cy = cy - this.y;
		
		if (cw < 0) cw = this.data[0] + cw;
		if (ch < 0) ch = this.data[1] + ch;
		
		int rgb[] = new int[2 + cw * ch];
		rgb[0] = cw;
		rgb[1] = ch;
		int index = 2;
		
		for(int j = 0; j < ch; j++) {
			for(int i = 0; i < cw; i++) {
				rgb[index++] = this.data[2 + (j + cy) * this.data[0] + (i + cw)];
			}
		}
		
		return rgb;
	}
	
	public String getURI() { return this.uri; }
	public void setURI(String uri) { this.uri = uri; }
	public Object getData() { return this.data; }

	public void saveLocal(String file) throws IOException {
	}
	
	public boolean equals(Object o) {
		if (o instanceof ImageResource == false) return false;
		
		ImageResource res = (ImageResource) o;
		if (this.w == res.w &&
			this.h == res.h &&
			this.x == res.x &&
			this.y == res.y &&
			this.uri.equals(res.uri)) return true;
		
		return false;
	}
	
	public int hashCode() { return this.uri.hashCode(); }

	public InputStream getInputStream() {
		throw new NotImplementedException();
//		return null;
	}
}
