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
