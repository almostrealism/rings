package io.almostrealism.controls;

import org.almostrealism.html.Div;
import org.almostrealism.html.HTMLString;

public class SectionHeader extends Div {
	public SectionHeader(String title, String target) {
		add(new HTMLString("<b>" + title + "</b> <button data-toggle=\"collapse\" data-target=\"#" + target + "\">\u25B7</button>"));
	}
}
