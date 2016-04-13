package com.almostrealism.ui;

import java.awt.Font;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * @author  Michael Murray
 */
public class ScrollingTextDisplay extends JLabel implements Runnable {
	public static interface TextProducer { public String nextPhrase(); }
	
	private TextProducer producer;
	private String text, display;
	private int col;
	private int sleep;
	
	public ScrollingTextDisplay(TextProducer p, int col) {
		this.producer = p;
		this.col = col;
		
		super.setFont(new Font("Monospaced", Font.BOLD, 12));
		
		this.setSleep(160);
		
		this.text = p.nextPhrase();
		
		Thread t = new Thread(this);
//		t.setDaemon(true);
		t.start();
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(this.sleep);
			} catch (InterruptedException ie) { }
			
			if (this.text.length() > 0) this.text = this.text.substring(1);
			
			if (this.text.length() > this.col) {
				this.display = this.text.substring(0, this.col);
			} else {
				StringBuffer s = new StringBuffer();
				for (int i = 0; i < this.col / 2; i++) s.append(" ");
				s.append(this.producer.nextPhrase());
				
				this.text = this.text.concat(s.toString());
				this.display = this.text;
			}
			
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						ScrollingTextDisplay.this.setText(ScrollingTextDisplay.this.display);
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return  The sleep time between each text shift in msecs.
	 */
	public int getSleep() { return sleep; }
	
	/**
	 * Sets the sleep time between each text shift.
	 * 
	 * @param sleep  The sleep time in msecs.
	 */
	public void setSleep(int sleep) { this.sleep = sleep; }
}
