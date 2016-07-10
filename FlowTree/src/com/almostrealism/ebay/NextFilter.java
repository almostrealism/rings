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
