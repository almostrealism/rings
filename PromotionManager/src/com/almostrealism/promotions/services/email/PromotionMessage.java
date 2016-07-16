package com.almostrealism.promotions.services.email;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.almostrealism.promotions.entities.PromotionMolecule;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.services.EngagementService;

/**
 * A {@link PromotionMessage} is used by the email {@link EngagementService}
 * to send outgoing mail to a {@link Target}.
 * 
 * @author  Michael Murray
 */
public class PromotionMessage extends PromotionMolecule {
	private EmailAccount account;
	private Target target;
	
	private String subject;
	private String content;
	
	/**
	 * Create a new {@link PromotionMessage} for the specified {@link Target}.
	 * The specified {@link EmailAccount} will be used to construct a JavaMail
	 * email message when the {@link #getMailMessage()} method is called.
	 */
	public PromotionMessage(EmailAccount e, Target t) {
		this.account = e;
		this.target = t;
	}
	
	/**
	 * Constructs a JavaMail mail {@link Message} for this {@link PromotionMessage}.
	 */
	public Message getMailMessage() throws AddressException, MessagingException {
		Session session = this.account.getSession();
		
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(account.getFromAddress()));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(target.getContactEmail()));
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(content);
		
		return msg;
	}
}
