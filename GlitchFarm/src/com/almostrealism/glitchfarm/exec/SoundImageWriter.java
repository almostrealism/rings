package com.almostrealism.glitchfarm.exec;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.almostrealism.glitchfarm.obj.Sample;
import com.almostrealism.glitchfarm.transform.LinearResampleTransformer;
import com.almostrealism.glitchfarm.util.SampleIOUtilities;

public class SoundImageWriter {
	private static int mod = 1024;
	private static DecimalFormat format = new DecimalFormat("000000");

	public static void main(String args[]) throws UnsupportedAudioFileException, IOException {
		FileInputStream in = new FileInputStream(args[0]);
		BufferedInputStream bin = new BufferedInputStream(in);

		Sample s = SampleIOUtilities.loadSample(bin);
		
		System.out.println("Sample Loaded from " + args[0]);
		
		// Scale the sample
		LinearResampleTransformer transform = new LinearResampleTransformer();
//		transform.setRatio(1.0 / 10.0);
//		transform.transform(s);
//		
//		System.out.println("Sample transformed by " + transform.getRatio());

		BufferedImage currentImage = null;
		Graphics g = null;
		
		int w = mod / 2;
		int index = -1;
		double r = 1.0;
		Color scolor = Color.black;
		boolean aggregate = true;

		for (int i = 0; i < s.data.length; i++) {
			if (i % mod == 0) {
				if (currentImage != null) {
					try {
						File outputfile = new File("output" + format.format(i / mod) + ".png");
						ImageIO.write(currentImage, "png", outputfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				currentImage = new BufferedImage(mod, 256 * s.data[0].length, BufferedImage.TYPE_INT_RGB);
				g = currentImage.getGraphics();
				
				g.setColor(Color.white);
				g.fillRect(0, 0, mod, 256 * s.data[0].length);
				w = w / 2;
				
				g.setColor(scolor);
			}

			index = i;

//			if (index < 0) continue i;
//			if (index >= s.data.length) continue i;

//			if (index >= s.loopStart && index <= s.loopEnd) {
//				g.setColor(Color.green);
//				g.drawLine(i, 0, i, 256 * s.data[0].length);
//				g.setColor(scolor);
//			}
			
			g.setColor(scolor);
			
			if (aggregate) {
				int left = s.data[index][1];
				left = left << 8;
				left += s.data[index][0];
				left = left / 64;
				
				int right = s.data[index][3];
				right = right << 8;
				right += s.data[index][2];
				right = right / 64;
				
				g.drawLine(i % mod, (int) (256 * r), i % mod, (int) ((256 + left) * r));
				g.drawLine(i % mod, (int) (3 * 256 * r), i % mod, (int) ((256 * 3 + right) * r));
			} else {
				g.drawLine(i % mod, (int) (128 * r), i % mod, (int) ((128 + s.data[index][0]) * r));
	
				if (s.data[index].length > 1)
					g.drawLine(i % mod, (int) (3 * 128 * r), i % mod, (int) ((128 * 3 + s.data[index][1]) * r));
	
				if (s.data[index].length > 2)
					g.drawLine(i % mod, (int) (5 * 128 * r), i % mod, (int) ((128 * 5 + s.data[index][2]) * r));
	
				if (s.data[index].length > 3)
					g.drawLine(i % mod, (int) (7 * 128 * r), i % mod, (int) ((128 * 7 + s.data[index][3]) * r));
			}
	
	//			g.setColor(Color.red);
	
	//			for (int i = 0; i < sample.data[0].length; i++) {
	//				g.drawLine(0, (int) (r * 128 * (2 * i + 2)), 2 * w, (int) (r * 128 * (2 * i + 2)));
	//			}
		}
	}
}
