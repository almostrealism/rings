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
	private double scale;

	public MediaPreprocessor(double scale) {
		this.scale = scale;

		if (scale != 1.0) {
			at.scale(scale, scale);
			scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		}
	}

	protected BufferedImage convertFrameToBuffer(Frame f, boolean enableScale) {
		if (f == null) {
			System.out.println("WARN: Null frame");
			return null;
		}

		if (!f.getTypes().contains(Frame.Type.VIDEO)) return null;
		count++;

		try {
			BufferedImage before = new Java2DFrameConverter().convert(f);

			if (scaleOp == null || !enableScale) {
				return before;
			} else {
				int w = before.getWidth();
				int h = before.getHeight();
				BufferedImage after = new BufferedImage((int) (w * scale), (int) (h * scale), BufferedImage.TYPE_INT_ARGB);
				return scaleOp.filter(before, after);
			}
		} finally {
			if (count % 1000 == 0) {
				System.out.println("MediaPreprocessor: " + count + " frames processed");
			}
		}
	}
}
