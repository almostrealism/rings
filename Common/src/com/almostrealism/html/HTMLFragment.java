package com.almostrealism.html;

import java.util.ArrayList;

public class HTMLFragment extends ArrayList<HTMLContent> implements HTMLContent {
	public enum Type { HEAD, BODY, SCRIPT; }
	
	private Type type;
	
	public HTMLFragment() { this(Type.BODY); }
	
	public HTMLFragment(Type t) { this.type = t; }
	
	public Type getType() { return type; }
	
	public String toHTML() {
		StringBuffer buf = new StringBuffer();
		
		for (HTMLContent c : this) {
			buf.append(c.toHTML());
			buf.append("\n");
		}
		
		return buf.toString();
	}
}
