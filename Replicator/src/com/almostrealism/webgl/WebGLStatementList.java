package com.almostrealism.webgl;

import java.util.ArrayList;

import com.almostrealism.html.HTMLContent;
import com.almostrealism.html.HTMLFragment;

public class WebGLStatementList extends ArrayList<WebGLStatement> implements WebGLExportable {
	@Override
	public HTMLContent getWebGLContent() {
		HTMLFragment f = new HTMLFragment();
		for (WebGLStatement g : this) { f.add(g); }
		return f;
	}
}
