package net.sf.jrings.ebay;

import org.apache.slide.structure.LinkNode;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class HistoryFilter implements NodeFilter {
	public boolean accept(Node n) {
		if (n instanceof LinkTag == false) return false;
		
		LinkTag l = (LinkTag) n;
		return l.getLink().contains("?ViewBids");
	}
	
	public static void main(String args[]) throws ParserException {
		Parser p = new Parser("http://cgi.ebay.com/Yamaha-Electric-Keyboard-PSS-130-PortaSound_W0QQitemZ130145326532QQihZ003QQcategoryZ38091QQcmdZViewItem");
		NodeIterator itr = p.extractAllNodesThatMatch(new HistoryFilter()).elements();
		
		while (itr.hasMoreNodes()) {
			System.out.println(itr.nextNode());
		}
	}
}
