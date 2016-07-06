/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.flow.db;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * @author Mike Murray
 */
public class LoginDialog extends JPanel {
	private JFrame frame;
	private JTextField userField, passwdField;
	private JButton okButton, cancelButton;
	
	private String user, passwd;
	
	private boolean okayed, dismissed;
	
	public LoginDialog() {
		this.userField = new JTextField(20);
		this.passwdField = new JPasswordField(20);
		
		this.okButton = new JButton("Ok");
		this.cancelButton = new JButton("Cancel");
		
		this.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginDialog.this.user = LoginDialog.this.userField.getText();
				LoginDialog.this.passwd = LoginDialog.this.passwdField.getText();
				
				LoginDialog.this.okayed = true;
				LoginDialog.this.dismissed = true;
			}
		});
		
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginDialog.this.okayed = false;
				LoginDialog.this.dismissed = true;
			}
		});
		
		super.setLayout(new GridLayout(3, 2));
		
		super.add(new JLabel("User: "));
		super.add(this.userField);
		super.add(new JLabel("Password: "));
		super.add(this.passwdField);
		super.add(this.okButton);
		super.add(this.cancelButton);
		
		this.frame = new JFrame("Login");
		this.frame.setSize(250, 80);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.frame.setLocation((screen.width - this.frame.getWidth()) / 2,
				(screen.height - this.frame.getHeight()) / 2);
		this.frame.getContentPane().add(this);
	}
	
	public void showDialog(final Runnable r) {
		this.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				LoginDialog.this.frame.setVisible(false);
				r.run();
			}
		});
		
		this.frame.setVisible(true);
	}
	
	public String getUser() { return this.user; }
	public String getPassword() { return this.passwd; }
}
