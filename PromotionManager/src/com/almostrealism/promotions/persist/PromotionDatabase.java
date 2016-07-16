package com.almostrealism.promotions.persist;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.almostrealism.promotions.entities.Material;
import com.almostrealism.promotions.entities.Target;
import com.almostrealism.promotions.services.EngagementService;
import com.almostrealism.promotions.services.email.EmailAccount;

/**
 * The {@link PromotionDatabase} is used to persist everything related
 * to the promotional campaign, includeding {@link Target}s and {@link Material}.
 * 
 * @author  Michael Murray
 */
public class PromotionDatabase {
	private Collection<Target> targets = new ArrayList<Target>();
	private Collection<Material> material = new ArrayList<Material>();
	private Collection<EngagementService> services = new ArrayList<EngagementService>();
	
	public void setTargets(Collection<Target> targets) { this.targets = targets; }
	public void setMaterial(Collection<Material> material) { this.material = material; }
	public Collection<Target> getTargets() { return this.targets; }
	public Collection<Material> getMaterial() { return this.material; }
	
	public void addService(EngagementService s) { this.services.add(s); }
	public void setServices(Collection<EngagementService> services) { this.services = services; }
	public Collection<EngagementService> getServices() { return services; }
	
	/**
	 * Returns an {@link Action} that can be used to in the user interface
	 * to show a dialog for adding a new {@link EmailAccount} as an
	 * {@link EngagementService} to this {@link PromotionDatabase}.
	 */
	public Action getAddEmailServiceAction() {
		return new AbstractAction("Add Email Service...") {
			public void actionPerformed(ActionEvent e) {
				JComboBox protocolField = new JComboBox(new String[] {"POP3", "IMAP"});
				JTextField userField = new JTextField();
				JPasswordField passwordField = new JPasswordField();
				JTextField serverField = new JTextField();
				JTextField smtpServerField = new JTextField();
				
				JPanel configPanel = new JPanel(new GridLayout(0, 2));
				configPanel.add(new JLabel("Protocol: "));
				configPanel.add(protocolField);
				configPanel.add(new JLabel("User: "));
				configPanel.add(userField);
				configPanel.add(new JLabel("Password: "));
				configPanel.add(passwordField);
				configPanel.add(new JLabel("Server: "));
				configPanel.add(serverField);
				configPanel.add(new JLabel("SMTP Server: "));
				configPanel.add(smtpServerField);
				
				JOptionPane.showMessageDialog(null, configPanel, "New Email Account", JOptionPane.PLAIN_MESSAGE);
				
				EmailAccount a = new EmailAccount(String.valueOf(protocolField.getSelectedItem()),
													userField.getText(), new String(passwordField.getPassword()),
													serverField.getText(), smtpServerField.getText());
				addService(a);
			}
		};
	}
}
