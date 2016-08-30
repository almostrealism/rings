package com.almostrealism.promo.services.email;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.almostrealism.promo.analysis.AnalysisNews;
import com.almostrealism.promo.entities.PromotionAtom;
import com.almostrealism.promo.entities.Target;
import com.almostrealism.promo.services.EngagementService;
import com.almostrealism.promo.ui.EngagementEditor;

/**
 * {@link EmailAccount} implements an email based engagement service
 * for a particular email account hosted via POP3, IMAP, or similar
 * protocol available via the JavaMail API.
 * 
 * @author  Michael Murray
 */
public class EmailAccount implements EngagementService {
	private String type, user, password, server, smtpServer;
	private String from;
	private List<MailMessage> inbox;

	private Session session;

	/**
	 * Constructs a new email account with the specified credentials.
	 */
	public EmailAccount(String type, String user, String password, String server, String smtpServer) {
		this.type = type;
		this.user = user;
		this.password = password;
		this.server = server;
		this.smtpServer = smtpServer;
	}
	
	/**
	 * Returns a panel for editing an email message.
	 */
	public EngagementEditor getEditorPanel(final Target t) {
		return new EngagementEditor() {
			private JTextArea emailBodyArea;
			
			public void init() {
				super.setLayout(new BorderLayout());
				
				setMolecule(new PromotionMessage(EmailAccount.this, t));
				this.emailBodyArea = new JTextArea();
				add(new JScrollPane(emailBodyArea), BorderLayout.CENTER);
			}
			
			public void addPromotionAtom(PromotionAtom atom) {
				super.addPromotionAtom(atom);
				this.emailBodyArea.append(atom.getBody());
			}
		};
	}
	
	/**
	 * Connects to the mail server to retrieve new email and
	 * returns a collection of {@link MailMessage}s which were
	 * found in the inbox.
	 */
	public Collection<AnalysisNews> refresh() {
		connect();  // Refresh inbox

		Collection<AnalysisNews> news = new ArrayList<AnalysisNews>();
		news.addAll(inbox);
		return news;
	}
	
	/**
	 * Returns the session, if there is one. A session is created
	 * when the {@link #refresh()} method is called.
	 */
	public Session getSession() { return this.session; }
	
	/**
	 * Returns the address that should be used when sending outgoing
	 * promotional mail.
	 */
	public String getFromAddress() { return this.from; }

	/**
	 * Connect to the mail server to retrieve inbox contents
	 * so that {@link AnalysisNews} can be generated for new
	 * email messages.
	 */
	private void connect() {
		StringBuffer connectionUrl = new StringBuffer();
		connectionUrl.append(type + "://");
		connectionUrl.append(user + ":");
		connectionUrl.append(password + "@");
		connectionUrl.append(server + "/");

		// TODO progress bar impl here

		Store store = null;

		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", smtpServer);
			session = Session.getDefaultInstance(props, null);
			
			URLName urln = new URLName(connectionUrl.toString());
			store = session.getStore(urln);
			store.connect();
		} catch (Exception e) {
			showError("Problem connecting to mail server.", e);
		}
		
		try {
			Folder folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message messages[] = folder.getMessages();
			
			FetchProfile profile = new FetchProfile();
			profile.add(FetchProfile.Item.ENVELOPE);
			folder.fetch(messages, profile);
			
			
		} catch (Exception e) {
			showError("Unable to download messages", e);
		}
	}
	
	/**
	 * Send a JavaMail message for the specified {@link PromotionMessage}.
	 */
	private void sendMessage(int type, PromotionMessage m) {
		try {
			Message mail = m.getMailMessage();
			Transport.send(mail);
		} catch (Exception e) {
			showError("Unable to send email", e);
		}
	}
	
	/**
	 * Display a {@link JOptionPane} message dialog.
	 */
	private void showError(String message, Exception cause) {
		JOptionPane.showMessageDialog(null, message, "Email Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public String toString() { return "Email Account (" + user + "@" + server + ")"; }
}
