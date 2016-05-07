package net.sf.j3d.imaging;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.media.jai.JAI;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import com.almostrealism.io.FileEncoder;

import net.sf.j3d.ui.panels.PercentagePanel;
import net.sf.j3d.util.graphics.GraphicsConverter;
import net.sf.j3d.util.graphics.RGB;


public class ArtifactDetector {
	public static boolean verbose = false;
	
	private static int ignoreTop = 1800;
	private static double smooth = 0.3;
	private static boolean ui = true;
	
	private static RGB lastImg[][];
	private static RGB orig[][];
	private static ArtifactDetector detector;
	
	private static RGB highlight = new RGB(1.0, 1.0, 1.0);
	
	private boolean eliminateAvg = true;
	
	private int w, h;
	private RGB rgb[][];
	private double image[][];
	private double bright[][];
	private boolean emap[][];
	
	public static void main(String args[]) throws MalformedURLException {
		verbose = true;
		
		if (args.length < 1) {
			System.out.println("Please specify an image file to load.");
			System.exit(1);
		}
		
		if (args.length > 2) smooth = Double.parseDouble(args[2]);
		
		File imf = new File(args[0]);
		BufferedImage im = null;
		RGB rgb[][] = null;
		
		if (imf.isDirectory()) {
			File files[] = imf.listFiles();
			ui = false;
			
			for (int i = 0; i < files.length; i++) {
				args[0] = files[i].getPath();
				main(args);
			}
			
			System.exit(0);
		} else {
			System.out.print("Loading image: ");
			// JAI.disableDefaultTileCache();
			RenderedImage rim = JAI.create("fileload", args[0]);
			im = new BufferedImage(rim.getWidth(),
									rim.getHeight(),
									BufferedImage.TYPE_INT_RGB);
			im.setData(rim.getData());
			rim = null;
			System.out.println("Done");
			
			if (ui) {
				System.out.print("Converting image: ");
				rgb = GraphicsConverter.convertToRGBArray(im);
				System.out.println("Done");
				
				orig = new RGB[rgb.length][rgb[0].length];
				for (int i = 0; i < orig.length; i++) {
					for (int j = 0; j < orig[i].length; j++) {
						orig[i][j] = rgb[i][j];
					}
				}
				
				System.out.println("Initializing detector...");
				detector = new ArtifactDetector(rgb, rgb.length, rgb[0].length);
			}
		}
		
		int cols = (im.getWidth() / 100) + 1;
		if (im.getWidth() % 100 == 0) cols--;
		
		if (!ui) for (int k = 0; k < cols; k++) {
			System.gc();
			
			int top;
			if (args.length > 1 && args[1].equals("b"))
				top = 0;
			else
				top = ignoreTop;
			
			System.out.print("Converting image: ");
			rgb = GraphicsConverter.convertToRGBArray(
					im, k * 100, top,
					Math.min(100, im.getWidth() - k * 100),
					im.getHeight() - ignoreTop);
			System.out.println("Done");
			
			System.out.println("Initializing detector...");
			detector = new ArtifactDetector(rgb, rgb.length, rgb[0].length);
			
			if (!ui) {
				System.out.println("Scanning " + args[0] + ": ");
				// boolean e[][] = detector.scan(0.01, 0.25, 0.55, 0.95, 8, 2);
				// boolean e[][] = detector.scan(0.1, 0.2, 0.4, 0.95, 8, 2);
				// boolean e[][] = detector.scan(0.01, 0.1, 0.3, 1.0, 8, 2, 10);
				boolean e[][] = detector.scan(0.01, 0.08, 0.3, 1.0, 8, 2, 100);
				
				int tot = 0;
				boolean err = false;
				
				i: for (int i = 0; i < e.length; i++) {
					for (int j = 0; j < e[i].length; j++) {
						if (e[i][j]) tot++;
						
						if (tot > 2000) {
							err = true;
							break i;
						}
					}
				}
				
				if (err) {
					try {
						String output = args[0].substring(args[0].lastIndexOf("/") + 1);
						output = output.substring(0, output.lastIndexOf("."));
						output = output + "-" + k + "-err.jpeg";
						
						System.out.print("Writing " + output + ": ");
						FileEncoder.encodeImageFile(detector.getImage(),
													new File(output),
													FileEncoder.JPEGEncoding);
						System.out.println("Done");
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
		
		if (ui) {
			ImageIcon hic = new ImageIcon(
					GraphicsConverter.convertToAWTImage(detector.getSaturationMap()));
			final JFrame frame = new JFrame("Hue map");
			JPanel hp = new JPanel();
			hp.add(new JLabel(hic));
			JPanel ap = new JPanel();
			JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
											new JScrollPane(ap),
											new JScrollPane(hp));
			frame.getContentPane().add(sp);
			frame.setSize(800, 600);
			frame.setVisible(true);
			sp.setDividerLocation(0.8);
			
			ImageIcon aic = new ImageIcon(GraphicsConverter.convertToAWTImage(rgb));
			final JLabel display = new JLabel(aic);
			ap.add(display);
			
			final PercentagePanel mfield = new PercentagePanel();
			final PercentagePanel tfield = new PercentagePanel();
			final PercentagePanel sfield = new PercentagePanel();
			final PercentagePanel npfield = new PercentagePanel();
			final JTextField itfield = new JTextField(5);
			final JTextField nfield = new JTextField(5);
			final JTextField bfield = new JTextField(5);
			
			mfield.setValue(0.01);
			tfield.setValue(0.08);
			sfield.setValue(0.95);
			npfield.setValue(0.3);
			itfield.setText("8");
			nfield.setText("3");
			bfield.setText("100");
			
			JButton rbutton = new JButton("Refresh");
			rbutton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					double m = mfield.getValue();
					double t = tfield.getValue();
					double np = npfield.getValue();
					double s = sfield.getValue();
					int max = Integer.parseInt(itfield.getText());
					int et = Integer.parseInt(nfield.getText());
					int bound = Integer.parseInt(bfield.getText());
					
					detector.setImage(orig);
					detector.scan(m, t, np, s, max, et, bound);
					
					lastImg = detector.getImage();
					display.setIcon(new ImageIcon(GraphicsConverter.convertToAWTImage(lastImg)));
					frame.validate();
				}
			});
			
			JButton sbutton = new JButton("Save");
			sbutton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						FileEncoder.encodeImageFile(lastImg, new File("output.jpeg"),
													FileEncoder.JPEGEncoding);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			});
			
			JFrame cframe = new JFrame("Parameters");
			cframe.getContentPane().setLayout(new FlowLayout());
			cframe.getContentPane().add(mfield);
			cframe.getContentPane().add(tfield);
			cframe.getContentPane().add(sfield);
			cframe.getContentPane().add(npfield);
			cframe.getContentPane().add(itfield);
			cframe.getContentPane().add(nfield);
			cframe.getContentPane().add(bfield);
			cframe.getContentPane().add(rbutton);
			cframe.getContentPane().add(sbutton);
			cframe.setSize(260, 300);
			cframe.setLocation(850, 100);
			cframe.setVisible(true);
			
			frame.validate();
		}
	}
	
	public ArtifactDetector(RGB rgb[][], int w, int h) {
		this.rgb = rgb;
		
		this.w = w;
		this.h = h;
		
		this.image = new double[w][h];
		this.bright = new double[w][h];
		this.emap = new boolean[w][h];
		
		if (verbose) System.out.print("Generating hue map: ");
		
		double avg = 0.0;
		
		for (int i = 0; i < rgb.length; i++) {
			double lavg = 0.0;
			
			for (int j = 0; j < rgb[i].length; j++) {
				double r = 0.0, g = 0.0, b = 0.0;
				int t = 0;
				
				p: for (int p = i - 1; p <= i + 1; p++) {
					if (p < 0 || p >= rgb.length) continue p;
					
					q: for (int q = j - 1; q <= j + 1; q++) {
						if (i == p && j == q) continue q;
						if (q < 0 || q >= rgb[p].length) continue q;
						
						r += rgb[p][q].getRed();
						g += rgb[p][q].getGreen();
						b += rgb[p][q].getBlue();
						t++;
					}
				}
				
				float f[] = Color.RGBtoHSB(
						(int) (255 * (rgb[i][j].getRed() * (1.0 - smooth) + (r * smooth / t))),
						(int) (255 * (rgb[i][j].getGreen() * (1.0 - smooth) + (g * smooth / t))),
						(int) (255 * (rgb[i][j].getBlue() * (1.0 - smooth) + (b * smooth / t))),
						null);
				
				this.image[i][j] = f[0];
				this.bright[i][j] = f[1];
				lavg += this.image[i][j];
			}
			
			avg += lavg / this.image[i].length;
		}
		
		if (verbose) System.out.println("Done");
		
		avg = avg / this.image.length;
		if (verbose) System.out.println(avg);
		
		// avg = 0.1;
		
		if (this.eliminateAvg) {
			for (int i = 0; i < this.image.length; i++) {
				for (int j = 0; j < this.image[i].length; j++) {
					this.image[i][j] = Math.max(0.0, (this.image[i][j] - avg)) % 1.0;
				}
			}
		}
	}
	
	public boolean[][] scan(double m, double t, double np, double s, int max, int et, int boundBox) {
		double l = 0.0;
		int e = 0;
		
		for (int i = 0; i < this.image.length; i++) {
			j: for (int j = 0; j < this.image[i].length; j++) {
				if (this.bright[i][j] < 0.05) continue j;
				
				l = 10 * this.artifact(i, j, m, t, s, 0, max);
				
				if (l > 0.8) {
					e++;
					emap[i][j] = true;
				} else {
					emap[i][j] = false;
				}
			}
			
			if (i == this.image.length - 1 && (verbose || e > 0))
				System.out.println("Scanned line " + i + ", e = " + e);
		}
		
		int nr = 0;
		
		boolean em[][] = new boolean[this.emap.length][this.emap[0].length];
		
		i: for (int i = 0; i < this.image.length; i++) {
			if (et == 0) break i;
			
			j: for (int j = 0; j < this.image[i].length; j++) {
				em[i][j] = this.emap[i][j];
				if (!emap[i][j]) continue j;
				
				int x = 0;
				
				p: for (int p = i - et; p <= i + et; p++) {
					if (p < 0 || p >= this.image.length) continue p;
					
					q: for (int q = j - et; q <= j + et; q++) {
						if (q < 0 || q >= this.image[p].length) continue q;
						if (p == i && q == j) continue q;
						if (emap[p][q]) x++;
					}
				}
				
				double y = 2 * et + 1;
				
				if (x < np * y * y - 1) {
					em[i][j] = false;
					nr++;
				}
			}
		}
		
		emap = em;
		em = new boolean[this.emap.length][this.emap[0].length];
		
		for (int i = 0; i < boundBox; i++) {
			for (int j = 0; j < em.length; j++) {
				k: for (int k = 0; k < em[j].length; k++) {
					em[j][k] = this.emap[j][k];
					if (emap[j][k]) continue k;
					
					int x = 0;
					
					if (emap[j][Math.max(0, k - 1)]) x++;
					if (emap[Math.max(0, j - 1)][k]) x++;
					if (emap[j][Math.min(emap[j].length - 1, k + 1)]) x++;
					if (emap[Math.min(emap.length - 1, j + 1)][k]) x++;
					
					if (x >= 2) {
						em[j][k] = true;
						nr++;
					}
					
					// if (em[j][k]) this.rgb[j][k] = ArtifactDetector.highlight;
				}
			}
		}
		
		emap = em;
		em = new boolean[this.emap.length][this.emap[0].length];
		
		i: for (int i = 0; i < this.image.length; i++) {
			if (et == 0) break i;
			
			j: for (int j = 0; j < this.image[i].length; j++) {
				em[i][j] = this.emap[i][j];
				if (!emap[i][j]) continue j;
				
				int x = 0;
				
				p: for (int p = i - 1; p <= i + 1; p++) {
					if (p < 0 || p >= this.image.length) continue p;
					
					q: for (int q = j - 1; q <= j + 1; q++) {
						if (q < 0 || q >= this.image[p].length) continue q;
						if (p == i && q == j) continue q;
						if (emap[p][q]) x++;
					}
				}
				
				if (x <= 3) {
					em[i][j] = false;
					nr++;
				}
				
				if (em[i][j]) this.rgb[i][j] = ArtifactDetector.highlight;
			}
		}
		
		System.out.println("Eliminated " + nr + " pixels of noise.");
		
		return em;
	}
	
	public double artifact(int x, int y, double m, double t, double s, int it, int max) {
		if (it > max) return 0.0;
		
		double min = -1.0;
		int k = 0;
		
		double l = 0;
		
		if (x > 0 && y > 0) {
			l = image[x - 1][y - 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x - 1, y - 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (x > 0) {
			l = image[x - 1][y] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x - 1, y, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (x > 0 && y < h - 1) {
			l = image[x - 1][y + 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x - 1, y + 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (y > 0) {
			l = image[x][y - 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x, y - 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (y < h - 1) {
			l = image[x][y + 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x, y + 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (x < w - 1 && y > 0) {
			l = image[x + 1][y - 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x + 1, y - 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (x < w - 1) {
			l = image[x + 1][y] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x + 1, y, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		if (x < w - 1 && y < h - 1) {
			l = image[x + 1][y + 1] - image[x][y];
			
			if (l > s || (l < t && l > m)) {
				double d = artifact(x + 1, y + 1, m, t, s, it + 1, max);
				
				if (d >= 0 && (d + l < min || k <= 0)) {
					min = d + l;
					k++;
				}
			}
		}
		
		return Math.max(min, 0.0);
	}
	
	public void setImage(RGB img[][]) {
		for (int i = 0; i < this.rgb.length; i++) {
			for (int j = 0; j < this.rgb[i].length; j++) {
				this.rgb[i][j] = img[i][j];
			}
		}
	}
	
	public RGB[][] getImage() { return this.rgb; }
	
	public RGB[][] getHueMap() {
		RGB hue[][] = new RGB[this.image.length][this.image[0].length];
		
		for (int i = 0; i < hue.length; i++) {
			for (int j = 0; j < hue[i].length; j++) {
				hue[i][j] = new RGB(this.image[i][j],
									this.image[i][j],
									this.image[i][j]);
			}
		}
		
		return hue;
	}
	
	public RGB[][] getSaturationMap() {
		RGB br[][] = new RGB[this.bright.length][this.bright[0].length];
		
		for (int i = 0; i < br.length; i++) {
			for (int j = 0; j < br[i].length; j++) {
				br[i][j] = new RGB(this.bright[i][j],
									this.bright[i][j],
									this.bright[i][j]);
			}
		}
		
		return br;
	}
}
