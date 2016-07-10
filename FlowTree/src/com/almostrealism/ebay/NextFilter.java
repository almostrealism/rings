package com.almostrealism.ebay;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class NextFilter implements NodeFilter {
	public boolean accept(Node n) {
		n = n.getFirstChild();
		if (n == null)
			return false;
		else
			return n.getText().equals("Next");
	}
	
	public static void main(String args[]) throws ParserException {
		Parser p = new Parser("http://search.ebay.com/alienware");
		NodeIterator itr = p.extractAllNodesThatMatch(new NextFilter()).elements();
		
		while (itr.hasMoreNodes()) {
			System.out.println(itr.nextNode().getClass());
		}
	}
}
