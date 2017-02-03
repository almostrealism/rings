package io.almostrealism.controls;

import java.util.List;

import org.almostrealism.html.HTMLString;

public class ComboControl extends Control {
	public ComboControl(String name, List<String> choices) {
		String sname = name.replaceAll(" ", "");
		
		add(new HTMLString("<div class=\"col-xs-2\"></div>" +
							"<label class=\"col-xs-2 control-label\" for=\"select_" + sname + "\">" + name + "</label>" +
							"<div class=\"col-xs-8\">" +
							"<select id=\"select_" + sname + "\" name=\"select_" + sname + "\" class=\"form-control\"></select>" +
							getOptions(choices) + "</div>"));
	}
	
	public static String getOptions(Iterable<String> choices) {
		StringBuffer buf = new StringBuffer();
		
		for (String s : choices) {
			buf.append("<option>");
			buf.append(s);
			buf.append("</option>");
		}
		
		return buf.toString();
	}
}
