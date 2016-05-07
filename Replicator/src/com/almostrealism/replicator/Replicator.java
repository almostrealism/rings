package com.almostrealism.replicator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.almostrealism.io.WavefrontObjParser;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.receptor.Receptor;
import com.almostrealism.receptor.ui.SamplerPanel;
import com.almostrealism.replicator.ui.ReplicatorCanvas;
import com.almostrealism.replicator.ui.ReplicatorTableModel;

public class Replicator {
	private Receptor receptor;
	
	private ReplicatorCanvas canvas;
	private ReplicatorTableModel model;
	
	private JPanel controlPanel;
	
	private JFrame layersFrame, samplerFrame;
	
	public Replicator() throws UnsupportedAudioFileException, IOException {
		canvas = new ReplicatorCanvas();
		receptor = new Receptor();
		
		model = new ReplicatorTableModel();
		
		controlPanel = new JPanel(new GridLayout(0, 1));
		
		JButton layersButton = new JButton("Layers");
		layersButton.addActionListener((e) -> { showLayersFrame(0, 0); });
		controlPanel.add(layersButton);
		
		JButton samplerButton = new JButton("Sampler");
		samplerButton.addActionListener((e) -> { showSamplerFrame(0, 0); });
		controlPanel.add(samplerButton);
		
		JButton receptorButton = new JButton("Receptor");
		receptorButton.addActionListener((e) -> { receptor.showReceptorFrame(0, 0); });
		controlPanel.add(receptorButton);
		
		JButton feedbackButton = new JButton("Feedback");
		feedbackButton.addActionListener((e) -> { receptor.showFeedbackFrame(0, 0); });
		controlPanel.add(feedbackButton);
		
		layersFrame = new JFrame("Layers");
		layersFrame.setLayout(new BorderLayout());
		layersFrame.getContentPane().add(new JTable(model));
		layersFrame.setSize(300, 200);
		
		samplerFrame = new JFrame("Sampler");
		samplerFrame.getContentPane().setLayout(new BorderLayout());
		samplerFrame.getContentPane().add(new SamplerPanel(4, 4), BorderLayout.CENTER);
		samplerFrame.setSize(400, 400);
	}
	
	public void addLayer(String name, Surface s) { model.addLayer(name, s); }
	
	public ReplicatorCanvas getCanvas() { return canvas; }
	
	public JPanel getControlPanel() { return controlPanel; }
	
	public void showLayersFrame(int x, int y) {
		layersFrame.setLocation(x, y);
		layersFrame.setVisible(true);
	}
	
	public void showSamplerFrame(int x, int y) {
		samplerFrame.setLocation(x, y);
		samplerFrame.setVisible(true);
	}
	
	public static void main(String args[]) throws UnsupportedAudioFileException, IOException {
		Replicator r = new Replicator();
		
		JFrame canvasFrame = new JFrame("Replicant");
		canvasFrame.setLayout(new BorderLayout());
		canvasFrame.getContentPane().add(r.getCanvas());
		canvasFrame.setSize(400, 400);
		canvasFrame.setLocationRelativeTo(null);
		canvasFrame.setVisible(true);
		
		JFrame controlFrame = new JFrame("");
		controlFrame.setLayout(new BorderLayout());
		controlFrame.getContentPane().add(r.getControlPanel());
		controlFrame.setLocation(canvasFrame.getLocation().x + canvasFrame.getWidth(), canvasFrame.getLocation().y);
		controlFrame.setSize(90, 140);
		controlFrame.setVisible(true);
		
		r.addLayer("Cube", new WavefrontObjParser(Replicator.class.getResourceAsStream("/models/Cube.obj")).getMesh());
	}
}
