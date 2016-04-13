/*
 * Copyright (C) 2007  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.jrings.ebay;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import net.sf.j3d.network.Job;
import net.sf.j3d.network.db.Client;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

// TODO  Fix problem with 'Buy It Now' cost showing up as shipping cost.

/**
 * @author  Mike Murray
 */
public class EBayParserJob implements Job {
	private String uri;
	private long taskId;
	private String taskString;
	private String file;
	private boolean wrote;
	
	private PrintStream out;
	
	public EBayParserJob() { }
	
	public EBayParserJob(String uri, long taskId, String file, String taskString) {
		this.uri = uri;
		this.taskId = taskId;
		this.file = file;
		this.taskString = taskString;
	}
	
	/**
	 * @see net.sf.j3d.network.Job#encode()
	 */
	public String encode() {
		StringBuffer buf = new StringBuffer();
		
		buf.append(this.getClass().getName());
		buf.append(":uri=");
		buf.append(this.uri);
		buf.append(":task=");
		buf.append(this.taskString);
		buf.append(":file=");
		buf.append(this.file);
		buf.append(":id=");
		buf.append(this.taskId);
		
		return buf.toString();
	}

	/**
	 * @see net.sf.j3d.network.Job#getTaskId()
	 */
	public long getTaskId() { return this.taskId; }

	/**
	 * @see net.sf.j3d.network.Job#getTaskString()
	 */
	public String getTaskString() { return this.taskString; }

	/**
	 * @see net.sf.j3d.network.Job#set(java.lang.String, java.lang.String)
	 */
	public void set(String key, String value) {
		if (key.equals("uri")) {
			this.uri = value;
		} else if (key.equals("task")) {
			this.taskString = value;
		} else if (key.equals("file")) {
			this.file = value;
		} else if (key.equals("id")) {
			this.taskId = Long.parseLong(value);
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			OutputStream o = 
				Client.getCurrentClient().getOutputStream(this.file);
			this.out = new PrintStream(o);
			
			Parser p = new Parser(this.uri);
			NodeList l = p.extractAllNodesThatMatch(new HasAttributeFilter("class", "ens fontnormal"));
			
			NodeIterator itr = l.elements();
			while (itr.hasMoreNodes()) {
				Node n = itr.nextNode();
				LinkTag t = (LinkTag) n.getChildren().elementAt(0);
				this.getAuctionInfo(t.getAttribute("href"));
			}
			
			this.out.flush();
			this.out.close();
			
			if (!this.wrote) {
				System.out.println("EBayParserJob: Deleting empty file " + file);
				Client.getCurrentClient().deleteResource(file);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void getAuctionInfo(String href) {
		try {
			this.out.println("<item uri=\"" + href + "\">");
			this.wrote = true;
			
			Parser p = new Parser(href);
			NodeList l = p.extractAllNodesThatMatch(new PriceFilter());
			NodeIterator itr = l.elements();
			
			if (itr.hasMoreNodes())
				this.out.println("\t<price>" + itr.nextNode().getText() + "</price>");
			
			if (itr.hasMoreNodes())
				this.out.println("\t<shipping>" + itr.nextNode().getText() + "</shipping>");
			
			p = new Parser(href);
			l = p.extractAllNodesThatMatch(new HasAttributeFilter("class", "itemSpecifics"));
			itr = l.elements();
			while (itr.hasMoreNodes()) {
				Node n = itr.nextNode();
				this.out.println("\t<spec>" + n.getFirstChild().getText() + "</spec>");
			}
			
			p = new Parser(href);
			l = p.extractAllNodesThatMatch(new HistoryFilter());
			itr = l.elements();
			
			if (itr.hasMoreNodes()) {
				Node n = itr.nextNode().getFirstChild();
				this.out.println("<history>" + n.getText().trim() + "</history>");
			}
			
			p = new Parser(href);
			l = p.extractAllNodesThatMatch(new StartingTimeFilter());
			itr = l.elements();
			
			if (itr.hasMoreNodes()) {
				String s = itr.nextNode().getLastChild().getFirstChild().getText();
				this.out.println("<startTime>" + s.trim() + "</startTime>");
			}
			
			p = new Parser(href);
			l = p.extractAllNodesThatMatch(new DurationFilter());
			itr = l.elements();
			
			if (itr.hasMoreNodes()) {
				String s = itr.nextNode().getLastChild().getFirstChild().getText();
				this.out.println("<duration>" + s.trim() + "</duration>");
			}
		} catch (EncodingChangeException ec) {
			System.out.println("EBayParserJob: Encoding change (" + ec.getMessage() + ")");
			System.out.println("EBayParserJob: " + href + " may not have been properly parsed.");
		} catch (ParserException e) {
			e.printStackTrace();
		} catch (NoSuchElementException nse) {
			nse.printStackTrace();
		}
		
		this.out.println("</item>");
	}
	
	public NodeIterator getChildren(NodeIterator itr, String starts) throws ParserException {
		while (itr.hasMoreNodes()) {
			Node n = itr.nextNode();
			if (n.getText().startsWith(starts))
				return n.getChildren().elements();
		}
		
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EBayParserJob j = new EBayParserJob();
		j.set("uri", args[0]);
		j.run();
	}

}
