package com.almostrealism.audio.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.almostrealism.audio.JavaAudioSample;
import org.almostrealism.audio.OutputLine;

public class SampleDisplayPane extends JPanel implements ActionListener,
														MouseListener,
														MouseMotionListener,
														KeyListener {
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	
	protected static final int ZOOM1 = 20;
	protected static final int ZOOM2 = 100;
	protected static final int ZOOM3 = 400;
	protected static final int ZOOM4 = 1000;
	
	private JavaAudioSample sample;
	private OutputLine line;
	
	private JButton playButton;
	private JToggleButton zoomOneButton, zoomTwoButton, zoomThreeButton, zoomFourButton;
	private JLabel bpmLabel;
	
	private JPanel samplePanel;
	
	private KeyBoardSampleDisplay keyboardSamples;
	private SampleRowColumnDisplay columnDisplay;
	
	private int displayHeight = -1;
	private int orientation = HORIZONTAL;
	
	private int dsr = ZOOM2;
	private boolean scroll = false;
	private boolean editing = false;
	private boolean editMirroring = true;
	private boolean displayBeatMarkers = true;
	private boolean beatSelection = true;
	
	public SampleDisplayPane(JavaAudioSample s) {
		this(s, null, false);
	}
	
	public SampleDisplayPane(JavaAudioSample s, OutputLine line) {
		this(s, line, false);
	}
	
	public SampleDisplayPane(JavaAudioSample s, OutputLine line, boolean showControls) {
		super(new BorderLayout());
		
		this.sample = s;
		this.line = line;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		this.samplePanel = new JPanel() {
			public void paint(Graphics g) {
				SampleDisplayPane.this.paintThis(g);
			}
			
			public Dimension getPreferredSize() {
				if (displayHeight > 0) {
					return new Dimension(sample.data.length / dsr, displayHeight);
				} else {
					return new Dimension(sample.data.length / dsr, sample.data[0].length * 10);
				}
			}
		};
		
		this.add(this.samplePanel, BorderLayout.CENTER);
		
		if (showControls) {
			this.zoomOneButton = new JToggleButton("1");
			this.zoomTwoButton = new JToggleButton("2");
			this.zoomThreeButton = new JToggleButton("3");
			this.zoomFourButton = new JToggleButton("4");
			
			this.bpmLabel = new JLabel("? BPM");
			
			this.zoomTwoButton.setSelected(true);
			
			this.zoomOneButton.addActionListener(this);
			this.zoomTwoButton.addActionListener(this);
			this.zoomThreeButton.addActionListener(this);
			this.zoomFourButton.addActionListener(this);
			
			ButtonGroup g = new ButtonGroup();
			g.add(this.zoomOneButton);
			g.add(this.zoomTwoButton);
			g.add(this.zoomThreeButton);
			g.add(this.zoomFourButton);
			
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			buttonPanel.add(this.zoomOneButton);
			buttonPanel.add(this.zoomTwoButton);
			buttonPanel.add(this.zoomThreeButton);
			buttonPanel.add(this.zoomFourButton);
			buttonPanel.add(new JLabel("     "));
			buttonPanel.add(this.bpmLabel);
			
			this.playButton = new JButton("Play");
			this.playButton.addActionListener(this);
			this.add(buttonPanel, BorderLayout.NORTH);
			this.add(this.playButton, BorderLayout.SOUTH);
		}
	}
	
	public void setSample(JavaAudioSample s) { this.sample = s; this.repaint(); }
	public JavaAudioSample getSample() { return this.sample; }
	
	public void setLine(OutputLine line) { this.line = line; }
	
	public void setDownsampleRate(int dsr) { this.dsr = dsr; }
	public double getDownsampleRate() { return this.dsr; }
	
	public void setDisplayHeight(int height) { this.displayHeight = height; }
	public void setOrientation(int orient) { this.orientation = orient; }
	
	public void setScrollingDisplay(boolean scroll) { this.scroll = scroll; }
	
	public void setColumnDisplay(SampleRowColumnDisplay rcd) {
		this.columnDisplay = rcd;
		this.setKeyBoardSampleDisplay(rcd.getKeyBoardSampleDisplay());
	}
	
	public void setKeyBoardSampleDisplay(KeyBoardSampleDisplay kd) {
		this.keyboardSamples = kd;
	}
	
	public KeyBoardSampleDisplay getKeyBoardSampleDisplay() {
		return this.keyboardSamples;
	}
	
	/**
	 * Updates the information displayed in the control area of this
	 * SampleDisplayPane to match that of the sample. This includes
	 * BPM listing.
	 */
	public void updateControlInfo() {
		if (this.sample.isBpmSet()) {
			this.bpmLabel.setText(this.sample.getBPM() + " BPM");
		} else {
			this.bpmLabel.setText("? BPM");
		}
	}
	
	public void paintThis(Graphics g) {
		int w = getWidth();
		
		g.setColor(Color.black);
		g.fillRect(0, 0, w, getHeight());
		
		if (this.sample == null) return;
		
		double r = getVerticalScaleFactor();
		
		int pos = sample.pos;
		int index = -1;
		w = w / 2;
		
		Color scolor = Color.blue;
		if (sample.isMuted()) scolor = Color.white;
		
		g.setColor(scolor);
		i: for (int i = 0; i < (w * 2); i++) {
			if (scroll)
				index = pos + (i - w) * dsr;
			else
				index = i * dsr;
			
			if (index < 0) continue i;
			if (index >= sample.data.length) continue i;
			
			if (i == w && scroll) {
				g.setColor(Color.red);
				g.drawLine(i, 0, i, getHeight());
				g.setColor(scolor);
			} else if (index >= sample.loopStart && index <= sample.loopEnd) {
				g.setColor(Color.green);
				g.drawLine(i, 0, i, getHeight());
				g.setColor(scolor);
			}
			
			g.drawLine(i, (int) (128 * r), i, (int) ((128 + sample.data[index][0]) * r));
			
			if (sample.data[index].length > 1)
				g.drawLine(i, (int) (3 * 128 * r), i, (int) ((128 * 3 + sample.data[index][1]) * r));
			
			if (sample.data[index].length > 2)
				g.drawLine(i, (int) (5 * 128 * r), i, (int) ((128 * 5 + sample.data[index][2]) * r));
			
			if (sample.data[index].length > 3)
				g.drawLine(i, (int) (7 * 128 * r), i, (int) ((128 * 7 + sample.data[index][3]) * r));
			
			if (displayBeatMarkers && sample.marker >= 0) {
				int d = (index - sample.marker) % sample.beatLength;
				if (d < this.dsr) {
					g.setColor(Color.red);
					g.drawLine(i, 0, i, getHeight());
					g.setColor(scolor);
				}
			}
		}
		
		g.setColor(Color.red);
		
		for (int i = 0; i < sample.data[0].length; i++) {
			g.drawLine(0, (int) (r * 128 * (2 * i + 2)), 2 * w, (int) (r * 128 * (2 * i + 2)));
		}
	}
	
	public double getVerticalScaleFactor() {
		return getHeight() / (256.0 * sample.data[0].length);
	}
	
	public int getPos(int x) {
		if (this.scroll)
			return sample.pos + dsr * (x - getWidth() / 2);
		else
			return x * dsr;
	}
	
	public int[] getVerticalValue(int y) {
		double r = getVerticalScaleFactor();
		
		int value[] = new int[2];
		
		i: for (int i = 1; ; i++) {
			if (y < r * 256 * i) {
				value[0] = i - 1;
				break i;
			}
		}
		
		int delta = y % (int) (256 * r);
		value[1] = (int) (delta / r - 128);
		return value;
	}
	
	public void toggleMute() {
		this.sample.toggleMute();
		this.repaint();
	}
	
	public void startEditing() { this.editing = true; }
	public void stopEditing() { this.editing = false; }
	public boolean toggleEditing() { return this.editing = !this.editing; }
	public boolean isEditing() { return this.editing; }
	
	public void removeMouseListener() { this.removeMouseListener(this); }
	
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			MixerMenus.currentSample = this.sample;
			MixerMenus.currentDisplayPane = this;
			MixerMenus.showAddMenu(this, e.getX(), e.getY());
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.playButton) {
			if (this.sample.isStopped())
				this.sample.playThread(this.line).start();
			else
				this.sample.stop();
		} else if (e.getSource() == this.zoomOneButton) {
			this.dsr = ZOOM1;
			updateSize();
		} else if (e.getSource() == this.zoomTwoButton) {
			this.dsr = ZOOM2;
			updateSize();
		} else if (e.getSource() == this.zoomThreeButton) {
			this.dsr = ZOOM3;
			updateSize();
		} else if (e.getSource() == this.zoomFourButton) {
			this.dsr = ZOOM4;
			updateSize();
		}
	}
	
	protected void updateSize() {
		Component c = this;
		
		while (c != null) {
			if (c instanceof JScrollPane) {
				((JScrollPane)c).setViewportView(this);
				return;
			}
			c = c.getParent();
		}
		
		if (getParent() != null)
			getParent().doLayout();
		
		repaint();
	}
	
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	
	public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if (this.sample == null) return;
		if (this.editing) return;
		
		if (this.columnDisplay != null && this.keyboardSamples != null) {
			int key = this.columnDisplay.getKeyBoardSampleIndex();
			
			if (key > 0) {
				this.keyboardSamples.setSample(key, new JavaAudioSample(this.sample.data, this.sample.getFormat()));
				return;
			}
		}
		
		if ((e.getModifiers() & MouseEvent.ALT_MASK) != 0) {
			this.toggleMute();
		} else if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0) {
			this.sample.loopEnd = Math.min(this.getPos(e.getX()), sample.data.length);
		} else {
			this.sample.loopStart = Math.max(this.getPos(e.getX()), 0);
		}
		
		this.repaint();
	}

	public void mouseReleased(MouseEvent e) { }

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (this.sample.isStopped())
				this.sample.playThread(this.line).start();
			else
				this.sample.stop();
		}
	}

	public void keyReleased(KeyEvent e) { }
	public void keyTyped(KeyEvent e) { }

	public void mouseDragged(MouseEvent e) {
		if (!this.editing) return;
		
		int x = this.getPos(e.getX());
		int vert[] = this.getVerticalValue(e.getY());
		
		if (x < 0 || x >= this.sample.data.length) return;
		
		if (vert[0] >= this.sample.data[x].length) return;
		
		int tot = this.getPos(e.getX() + 1);
		
		for ( ; x < tot; x++) {
			this.sample.data[x][vert[0]] = (byte) vert[1];
			if (editMirroring && this.sample.data[x].length > vert[0] * 2)
				this.sample.data[x][vert[0] * 2] = (byte) vert[1];
		}
		
		this.repaint();
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
