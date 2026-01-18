/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.photon.xml;

import com.almostrealism.photon.ui.DefaultProbabilityDistributionEditPanel;
import com.almostrealism.stats.OverlayBRDF;
import io.almostrealism.tree.ui.ObjectTreeDisplay;
import io.almostrealism.tree.ui.ObjectTreeNode;
import org.almostrealism.color.ProbabilityDistribution;
import org.almostrealism.color.RGB;
import org.almostrealism.texture.GraphicsConverter;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ProbabilityDistributionDisplay extends JPanel
								implements NodeDisplay, TreeCellRenderer, MouseListener {
	private final Node node;
	private Object obj;
	private Color cl;
	private boolean arrow, open;
	
	private JFrame frame;
	private ObjectTreeDisplay display;
	private DefaultProbabilityDistributionEditPanel editPanel;
	
	public ProbabilityDistributionDisplay() { this(null); }
	
	public ProbabilityDistributionDisplay(Node n) {
		this.node = n;
		super.addMouseListener(this);
	}
	
	public Dimension getPreferredSize() { return new Dimension(200, 30); }
	
	public void paint(Graphics g) {
		super.paint(g);
		
		Object o = this.obj;
		if (o == null) o = this.node.getObject();
		if (!(o instanceof ProbabilityDistribution)) return;
		ProbabilityDistribution dist = (ProbabilityDistribution) o;
		
		int off = 1;
		double w = super.getWidth() * 3.0 / 4.0;
		int h = super.getHeight();
		
		if (this.arrow) {
			g.setColor(super.getParent().getBackground());
			g.fillRect(1, 1, 20, h - 2);
			
			w = w - 20;
			off = 21;
			g.setColor(this.cl);
			
			if (this.open) {
				g.drawLine(10, h / 2 - 5, 10, h / 2 + 5);
				g.drawLine(5, h / 2, 10, h / 2 + 5);
				g.drawLine(15, h / 2, 10, h / 2 + 5);
			} else {
				g.drawLine(5, h / 2, 15, h / 2);
			}
		}
		
		for (int i = 0; i < w; i++) {
			double n = 1000.0 * dist.getSample((double) i / w);
			Color c = GraphicsConverter.convertToAWTColor(new RGB(n));
			g.setColor(c);
			g.drawLine(off + i, 1, off + i, h - 2);
		}
		
		g.setColor(super.getParent().getBackground());
		g.fillRect((int) w + off, 1, 8, h - 2);
		int x = (int) (w + off + 8);
		g.setColor(GraphicsConverter.convertToAWTColor(dist.getIntegrated()));
		g.fillRect(x, 1, super.getWidth() - x - 1, h - 2);
	}
	
	public Container getFrame() { return null; }
	public Container getContainer() { return this; }
	public int getGridHeight() { return 1; }
	public int getGridWidth() { return 2; }
	public Node getNode() { return this.node; }
	
	public Object getObject() {
		if (this.obj != null)
			return this.obj;
		else
			return this.node.getObject();
	}
	
	public void setObject(Object o) { this.obj = o; }
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
												boolean selected, boolean expanded,
												boolean leaf, int row, boolean hasFocus) {
		try {
			this.obj = ((ObjectTreeNode) value).getObject();
		} catch (Exception e) {
			String c = e.getMessage();
			if (c == null || e.getCause() != null) c = e.getCause().getMessage();
			System.out.println("ProbabilityDistributionDisplay: " +
								"Could not render tree cell (" + c);
			this.obj = null;
		}
		
		this.arrow = true;
		this.open = expanded;
		
		Color bg = tree.getBackground();
		float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
		this.cl = new Color(Color.HSBtoRGB(1 - hsb[0], hsb[1], 1 - hsb[2]));
		
		if (selected)
			super.setBorder(BorderFactory.createLineBorder(this.cl));
		else
			super.setBorder(BorderFactory.createLineBorder(tree.getBackground()));
		
		return this;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (this.frame == null) {
			this.frame = new JFrame("Spectrum Editor");
			
			JPanel panel = null;
			
			Object o = this.getObject();
			
			if (o instanceof OverlayBRDF) {
				this.display = new ObjectTreeDisplay(new ObjectTreeNode(null, o, false));
				panel = this.display;
			} else {
				this.editPanel = new DefaultProbabilityDistributionEditPanel(
										(ProbabilityDistribution) o);
				panel = this.editPanel;
			}
			
			this.frame.setSize(300, 280);
			this.frame.getContentPane().add(panel);
		}
		
		if (this.display != null) this.display.updateDisplay();
		if (this.editPanel != null) this.editPanel.updateDisplay();
		
		this.frame.setVisible(true);
	}
	
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
}
