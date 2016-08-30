package com.almostrealism.promo.entities;

/**
 * The {@link URLPromotionAtom} provides a URL and optional comment for its
 * body.
 * 
 * @author  Michael Murray
 */
public class URLPromotionAtom implements PromotionAtom {
	private String name, url, comment;
	
	/**
	 * Create a new atom for the specified url and optional comment.
	 */
	public URLPromotionAtom(String name, String url, String comment) {
		this.name = name;
		this.url = url;
		this.comment = comment;
	}
	
	/**
	 * Return the name for this atom.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the comment, then some new lines, then the URL, then some new lines.
	 */
	public String getBody() {
		StringBuffer output = new StringBuffer();
		
		if (comment != null) {
			output.append(comment);
			output.append("\n\n");
		}
		
		output.append(url);
		output.append("\n\n\n");
		
		return output.toString();
	}
	
	public String toString() { return this.name; }
}
