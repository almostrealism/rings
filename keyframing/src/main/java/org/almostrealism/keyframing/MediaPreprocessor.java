package org.almostrealism.keyframing;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class MediaPreprocessor {
	private AffineTransform at = new AffineTransform();
	private AffineTransformOp scaleOp;
	private int count;

	public MediaPreprocessor() {
		at.scale(0.25, 0.25);
		scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	}

	protected BufferedImage convertFrameToBuffer(Frame f) {
		count++;

		try {
			BufferedImage before = new Java2DFrameConverter().convert(f);
			int w = before.getWidth();
			int h = before.getHeight();
			BufferedImage after = new BufferedImage(w / 4, h / 4, BufferedImage.TYPE_INT_ARGB);
			return scaleOp.filter(before, after);
		} finally {
			if (count % 100 == 0) {
				System.out.println("MediaPreprocessor: " + count + " frames processed");
			}
		}
	}
}
