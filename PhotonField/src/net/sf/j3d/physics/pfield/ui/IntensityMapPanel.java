package net.sf.j3d.physics.pfield.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.j3d.physics.pfield.texture.CosineIntensityMap;
import net.sf.j3d.physics.pfield.texture.IntensityMap;
import net.sf.j3d.physics.pfield.texture.Turbulence;
import net.sf.j3d.ui.displays.ImageCanvas;
import net.sf.j3d.util.graphics.RGB;

public class IntensityMapPanel extends JPanel {
	private IntensityMap map;
	private ImageCanvas display;
	
	public static void main(String args[]) throws InstantiationException,
											IllegalAccessException,
											ClassNotFoundException {
		IntensityMap map;
		
		if (args.length > 0) {
			map = (IntensityMap) Class.forName(args[0]).newInstance();
		} else {
			map = new CosineIntensityMap(new Turbulence());
		}
		
		ImageCanvas canvas = new ImageCanvas(600, 600);
		IntensityMapPanel panel = new IntensityMapPanel(map, canvas);
		
		JFrame display = new JFrame("Intensity");
		JFrame frame = new JFrame("Map");
		
		display.getContentPane().add(canvas);
		frame.getContentPane().add(panel);
		
		display.setSize(250, 600);
		frame.setSize(100, 70);
		frame.setLocation(300, 30);
		
		display.setVisible(true);
		frame.setVisible(true);
	}
	
	public IntensityMapPanel(IntensityMap map, ImageCanvas display) {
		this.map = map;
		this.display = display;
		
		JButton updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				IntensityMapPanel.this.updateDisplay();
			}
		});
		
		super.add(updateButton);
	}
	
	public void updateDisplay() {
		int w = this.display.getWidth();
		int h = this.display.getHeight();
		RGB image[][] = new RGB[w][h];
		
		for (int i = 0; i < w; i++) {
			double u = ((double)i)/((double)w);
			
			for (int j = 0; j < h; j++) {
				double v = ((double)j)/((double)h);
				
				if (v == 0.0)
					v = 0.9999999999;
				else
					v = 1.0 - v;
				
				double in = this.map.getIntensity(u, v, 0.0);
				image[i][j] = new RGB(in, in, in);
			}
		}
		
		this.display.setImageData(image);
	}
}
