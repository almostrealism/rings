package com.almostrealism.promo.entities;

/**
 * A {@link Target} instance represents a particular organization or
 * individual that is the target of the running promotional campaign.
 * There are a variety of types of targets, which might be interacted
 * with in different ways depending on the style of campaign. These
 * types are collected in the {@link TargetType} enum.
 * 
 * Additional meta data associated with a {@link Target} is available via
 * getter and setter methods, which are also used in persisting the object
 * as a java bean. A collection of {@link Target}s is the platform for
 * any promotional campaign and all other operations in the promotions
 * system use this object to describe the scope of a particular promotional
 * event.
 * 
 * @author  Michael Murray
 */
public class Target {
	public static enum TargetType { Blog, Label, Performer; }
	
	/**
	 * The type of target
	 */
	private TargetType type;
	
	/**
	 * Name of target
	 */
	private String name;
	
	/**
	 * Contact email for the target
	 */
	private String contactEmail;
	
	/**
	 * The contact URL is the url of a page which will allow
	 * this target to be contacted over the web. This may be
	 * a soundcloud drop box, a contact form built into a
	 * web site, or any other location on the web that can
	 * be used to access the target for promotional purposes.
	 */
	private String contactUrl;
	
	/**
	 * This is the publicly facing URL for the target. For blogs,
	 * this would be the blog address. For other targets it may
	 * be a twitter, facebook, or other social media page unless
	 * a reasonably current and active web presence exists for
	 * the target outside of social media.
	 */
	private String consumerUrl;
	
	/**
	 * Any additional notes for this target
	 */
	private String notes;

	public TargetType getType() { return type; }

	public void setType(TargetType type) { this.type = type; }

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	public String getContactEmail() { return contactEmail; }

	public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

	public String getContactUrl() { return contactUrl; }

	public void setContactUrl(String contactUrl) { this.contactUrl = contactUrl; }

	public String getConsumerUrl() { return consumerUrl; }

	public void setConsumerUrl(String consumerUrl) { this.consumerUrl = consumerUrl; }

	public String getNotes() { return notes; }

	public void setNotes(String notes) { this.notes = notes; }
}
