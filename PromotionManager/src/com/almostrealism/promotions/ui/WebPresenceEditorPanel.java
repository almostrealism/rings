package com.almostrealism.promotions.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.almostrealism.promotions.entities.Material;
import com.almostrealism.promotions.entities.Material.MaterialURLType;

/**
 * The {@link WebPresenceEditorPanel} provides an editor for the web presence
 * information associated with a particular {@link Material} instance. "Web
 * presences" are URLs which might be included in promotional messages or
 * other engagement services to provide access to the material being promoted
 * as it exists on the web.
 * 
 * @author  Michael Murray
 */
public class WebPresenceEditorPanel extends JPanel implements ActionListener {
	private Material material;
	
	private JTextField soundcloudText, beatportText, junoText, zippyshareText;
	
	/**
	 * Constructs a new blank {@link WebPresenceEditorPanel}
	 */
	public WebPresenceEditorPanel() {
		super(new GridLayout(0, 2));
		
		setBorder(BorderFactory.createTitledBorder("Web Presences"));
		
		this.soundcloudText = new JTextField();
		this.beatportText = new JTextField();
		this.junoText = new JTextField();
		this.zippyshareText = new JTextField();
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		
		this.add(new JLabel("Soundcloud:"));
		this.add(soundcloudText);
		this.add(new JLabel("Beatport:"));
		this.add(beatportText);
		this.add(new JLabel("Juno Download:"));
		this.add(junoText);
		this.add(new JLabel("Zippyshare: "));
		this.add(zippyshareText);
		this.add(new JLabel());
		this.add(saveButton);
		
		this.setMaterial(null); // Disables fields until a valid material is available
	}
	
	public void setMaterial(Material m) {
		this.material = m;
		boolean editable = m != null;
		
		this.soundcloudText.setEditable(editable);
		this.beatportText.setEditable(editable);
		this.junoText.setEditable(editable);
		this.zippyshareText.setEditable(editable);
		
		this.soundcloudText.setText("");
		this.beatportText.setText("");
		this.junoText.setText("");
		this.zippyshareText.setText("");
		
		if (!editable) return;
		
		Hashtable<MaterialURLType, String> presences = this.material.getWebPresences();
		
		if (presences.containsKey(MaterialURLType.Soundcloud))
			this.soundcloudText.setText(presences.get(MaterialURLType.Soundcloud));
		
		if (presences.containsKey(MaterialURLType.Beatport))
			this.beatportText.setText(presences.get(MaterialURLType.Beatport));
		
		if (presences.containsKey(MaterialURLType.Juno))
			this.junoText.setText(presences.get(MaterialURLType.Juno));
		
		if (presences.containsKey(MaterialURLType.Zippyshare))
			this.zippyshareText.setText(presences.get(MaterialURLType.Zippyshare));
	}
	
	/**
	 * Action for the save button. This will commit the values
	 * in the panel to the {@link Material} object.
	 */
	public void actionPerformed(ActionEvent e) {
		Hashtable<MaterialURLType, String> presences = this.material.getWebPresences();
		
		if (presences.containsKey(MaterialURLType.Soundcloud))
			presences.remove(MaterialURLType.Soundcloud);
		
		if (this.soundcloudText.getText().length() > 0)
			presences.put(MaterialURLType.Soundcloud, soundcloudText.getText());
		
		if (presences.containsKey(MaterialURLType.Beatport))
			presences.remove(MaterialURLType.Beatport);
		
		if (this.beatportText.getText().length() > 0)
			presences.put(MaterialURLType.Beatport, beatportText.getText());
		
		if (presences.containsKey(MaterialURLType.Juno))
			presences.remove(MaterialURLType.Juno);
		
		if (this.junoText.getText().length() > 0)
			presences.put(MaterialURLType.Juno, junoText.getText());
		
		if (presences.containsKey(MaterialURLType.Zippyshare))
			presences.remove(MaterialURLType.Zippyshare);
		
		if (this.zippyshareText.getText().length() > 0)
			presences.put(MaterialURLType.Zippyshare, zippyshareText.getText());
	}
}
