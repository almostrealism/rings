package com.almostrealism.promo.services.email;

import javax.mail.Message;

import com.almostrealism.promo.analysis.AnalysisNews;
import com.almostrealism.promo.services.EngagementService;

/**
 * A {@link MailMessage} is retrieved when refreshing the {@link EmailAccount} service
 * (A JavaMail based {@link EngagementService}). This can be inspected by analysis tools
 * to determine new information related to the promotional campaign.
 * 
 * @author  Michael Murray
 */
public class MailMessage implements AnalysisNews {
	private Message message;
	
	/**
	 * Constructs a new {@link MailMessage} for the specified
	 * JavaMail API {@link Message} instance.
	 */
	public MailMessage(Message m) {
		this.message = m;
	}
}
