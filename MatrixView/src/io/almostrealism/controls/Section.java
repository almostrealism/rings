package io.almostrealism.controls;

import org.almostrealism.html.Div;

public class Section extends Div {
	public Section(String title, boolean group) {
		String contentId = title.replaceAll(" ", "") + "_content";
		if (!group) add(new SectionHeader(title, contentId));
	}
	
	public SectionComponent addComponent() {
		SectionComponent c = new SectionComponent();
		add(c);
		return c;
	}
}
