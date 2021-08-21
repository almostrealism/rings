package com.almostrealism.audio.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.almostrealism.audio.JavaAudioSample;

// TODO  Convert to JavaFX
public class SampleLoadProgressBar extends JPanel implements Runnable {
	private JavaAudioSample sample;
	private AudioInputStream input;
	private SampleDisplayPane display;
	private JProgressBar progress;
	
	private Thread loadThread;
	private int tot = 0;
	
	public SampleLoadProgressBar(JavaAudioSample s, AudioInputStream in, SampleDisplayPane display) {
		this.sample = s;
		this.input = in;
		this.display = display;

		int len = (int) in.getFrameLength();
		this.progress = new JProgressBar(0, len);
		
		this.add(this.progress);
		
		this.loadThread = new Thread(this);
	}
	
	public void start() { this.loadThread.start(); }
	
	public void run() {
		AudioFormat format = input.getFormat();
		
		int frameSize = format.getFrameSize();
		double frameRate = format.getFrameRate();
		
		System.out.println("Loading Sample: ");
		System.out.println("\t Frame size = " + frameSize);
		System.out.println("\t Frame rate = " + frameRate);
		System.out.println("\t " + input.getFrameLength() + " frames");
		
		byte data[][] = sample.data;
		
		for (tot = 0; tot < input.getFrameLength(); tot++) {
			try {
				input.read(data[tot]);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			try {
				SwingUtilities.invokeAndWait(() -> progress.setValue(tot));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		if (display != null) display.repaint();
	}
}
