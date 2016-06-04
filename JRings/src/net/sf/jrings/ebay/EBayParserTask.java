/*
 * Copyright (C) 2005-07  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.jrings.ebay;

import java.io.IOException;

import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.almostrealism.util.Help;

import net.sf.j3d.network.Job;
import net.sf.j3d.network.JobFactory;
import net.sf.j3d.network.Server;
import net.sf.j3d.network.resources.ConcatenatedResource;

/**
 * @author  Mike Murray
 */
public class EBayParserTask implements JobFactory, Help {
	private long id;
	private double pri = 1.0, complete;
	private String search;
	
	private boolean started;
	private String next;
	
	/**
	 * @see net.sf.j3d.network.JobFactory#createJob(java.lang.String)
	 */
	public Job createJob(String data) { return Server.instantiateJobClass(data); }

	/**
	 * @see net.sf.j3d.network.JobFactory#encode()
	 */
	public String encode() {
		StringBuffer buf = new StringBuffer();
		
		buf.append(this.getClass().getName());
		buf.append(":search=");
		buf.append(this.search);
		buf.append(":pri=");
		buf.append(this.pri);
		buf.append(":id=");
		buf.append(this.id);
		
		return buf.toString();
	}

	/**
	 * @see net.sf.j3d.network.JobFactory#getCompleteness()
	 */
	public double getCompleteness() { return complete; }

	/**
	 * @see net.sf.j3d.network.JobFactory#getName()
	 */
	public String getName() { return "EBayParserTask [" + this.search + "] (" + this.id + ")"; }

	/**
	 * @see net.sf.j3d.network.JobFactory#getPriority()
	 */
	public double getPriority() { return this.pri; }

	/**
	 * @see net.sf.j3d.network.JobFactory#getTaskId()
	 */
	public long getTaskId() { return this.id; }

	/**
	 * @see net.sf.j3d.network.JobFactory#isComplete()
	 */
	public boolean isComplete() { return (this.complete >= 1.0); }

	/**
	 * @see net.sf.j3d.network.JobFactory#nextJob()
	 */
	public Job nextJob() {
		if (this.search == null) return null;
		
		if (!started) {
			try {
				ConcatenatedResource.createConcatenatedResource(
									"/files/ebay/" + this.search + ".xml",
									"/files/ebay/" + this.search);
			} catch (IOException ioe) {
				System.out.println("EBayParserTask: Unable to create ConcatenatedResource at " +
									"/files/ebay/" + this.search + ".xml (" + ioe.getMessage() + ")");
			}
			
			this.started = true;
		}
		
		if (this.next == null) {
			this.next = "http://search.ebay.com/" + this.search.replace(" ", "%20");
		} else {
			try {
				System.out.println("Opening: " + this.next);
				Parser p = new Parser(this.next);
				NodeList l = p.extractAllNodesThatMatch(new NextFilter());
				
				if (l.size() <= 0 || l.elementAt(0) instanceof LinkTag == false) {
					this.complete = 1.0;
					return null;
				}
				
				LinkTag t = (LinkTag) l.elementAt(0);
				String href = t.getAttribute("href");
				
				if (href.startsWith("http:"))
					this.next = href;
				else if (href.startsWith("/"))
					this.next = "http://search.ebay.com" + href;
				else
					this.next = "http://search.ebay.com/" + href;
				
				System.out.println("Found Next: " + this.next);
			} catch (ParserException e) {
				throw new RuntimeException(e);
			}
		}
		
		String file = "/files/ebay/" + this.search + "/" + System.currentTimeMillis() + ".xml";
		return new EBayParserJob(this.next, this.id, file, this.getName());
	}
	
	/**
	 * @see net.sf.j3d.network.JobFactory#set(java.lang.String, java.lang.String)
	 */
	public void set(String key, String value) {
		if (key.equals("id"))
			this.id = Long.parseLong(value);
		else if (key.equals("pri"))
			this.pri = Double.parseDouble(value);
		else if (key.equals("search"))
			this.search = value.replace("+", "%20");
	}
	
	/**
	 * @see net.sf.j3d.network.JobFactory#setPriority(double)
	 */
	public void setPriority(double p) { this.pri = p; }
	
	public String toString() { return this.getName(); }

	public String getHelpInfo() {
		return "The EBayParserTask searches ebay.\n" +
				"The search criteria is specified in the search property.\n" +
				"As an example, to search for auctions on alienware laptops\n" +
				"you would execute the sendtask command with the search property\n" +
				"specified as follows:\n" +
				"\t sendtask -1 net.sf.jrings.ebay.EBayParserTask search=alienware%20laptops\n" +
				"Notice that the space has been replaced by a %20, the correct escape code.";
	}
}
