package com.almostrealism.ebay;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class DurationFilter implements NodeFilter {
	public boolean accept(Node n) {
//		return (n.getText() != null && n.getText().contains("Duration:"));
		
		n = n.getFirstChild();
		if (n == null) return false;
		n = n.getFirstChild();
		if (n == null) return false;
		
		NodeList l = n.getChildren();
		if (l == null) return false;
		NodeIterator itr = l.elements();
		
		try {
			while (itr.hasMoreNodes()) {
				Node c = itr.nextNode();
				if (c.getText() != null && c.getText().contains("Duration:"))
					return true;
			}
		} catch (ParserException e) {
		}
		
		return false;
	}
	
	public static void main(String args[]) throws ParserException {
		Parser p = new Parser("http://cgi.ebay.com/NIB-Novation-Xiosynth-Synthesizer-Midi-Keyboard_W0QQitemZ110160541214QQihZ001QQcategoryZ38071QQcmdZViewItem");
		NodeIterator itr = p.extractAllNodesThatMatch(new DurationFilter()).elements();
		
		while (itr.hasMoreNodes()) {
			String s = itr.nextNode().getLastChild().getFirstChild().getText();
			System.out.println(s);
		}
	}
}
