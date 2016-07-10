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

package com.almostrealism.ebay;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class StartingTimeFilter implements NodeFilter {
	public boolean accept(Node n) {
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
				if (c.getText() != null && c.getText().contains("Starting time:"))
					return true;
			}
		} catch (ParserException e) {
		}
		
		return false;
	}
	
	public static void main(String args[]) throws ParserException {
		Parser p = new Parser("http://cgi.ebay.com/Yamaha-Electric-Keyboard-PSS-130-PortaSound_W0QQitemZ130145326532QQihZ003QQcategoryZ38091QQcmdZViewItem");
		NodeIterator itr = p.extractAllNodesThatMatch(new StartingTimeFilter()).elements();
		
		while (itr.hasMoreNodes()) {
			String s = itr.nextNode().getLastChild().getFirstChild().getText();
			System.out.println(s);
		}
	}
}
