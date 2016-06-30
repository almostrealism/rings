package com.almostrealism.html;

import java.util.ArrayList;

public class HTMLFragment extends ArrayList<HTMLContent> implements HTMLContent {
	public String toHTML() {
		StringBuffer buf = new StringBuffer();
		
		for (HTMLContent c : this) {
			buf.append(c.toHTML());
			buf.append("\n");
		}
		
		return buf.toString();
	}
}
