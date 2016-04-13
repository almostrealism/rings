package net.sf.jrings.ebay;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

public class PriceFilter implements NodeFilter {

	public boolean accept(Node n) {
		return n.getText().startsWith("US $");
	}

}
