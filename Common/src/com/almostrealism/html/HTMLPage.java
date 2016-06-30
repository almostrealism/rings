package com.almostrealism.html;

import java.util.ArrayList;
import java.util.List;

public class HTMLPage implements HTMLContent {
	private List<HTMLFragment> head;
	private List<HTMLFragment> body;
	
	public HTMLPage() {
		head = new ArrayList<HTMLFragment>();
		body = new ArrayList<HTMLFragment>();
	}
	
	public void add(HTMLFragment f) {
		if (f.getType() == HTMLFragment.Type.HEAD) {
			head.add(f);
		} else {
			body.add(f);
		}
	}
	
	@Override
	public String toHTML() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("<html>\n");
		buf.append("<head>\n");
		
		for (HTMLFragment f : head) {
			buf.append(f.toHTML());
			buf.append("\n");
		}
		
		buf.append("</head>\n");
		buf.append("<body>\n");
		
		for (HTMLFragment f : head) {
			buf.append(f.toHTML());
			buf.append("\n");
		}
		
		buf.append("</body>\n");
		buf.append("</html>");
		
		return buf.toString();
	}
}
