package io.almostrealism.controls;

import org.almostrealism.html.HTMLString;

public class ListControl extends Control {
	public ListControl(String name) {
		add(new HTMLString("<input list=\"" + name + "\">"));
		add(new HTMLString("<datalist id=\"" + name + "\">"));
		add(new HTMLString("</datalist>"));
	}
}
