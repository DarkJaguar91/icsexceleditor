package Image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ImageProcessor {

	/**
	 * Generates a histogram (colour) for an image
	 * 
	 * @param image
	 *            The image to use
	 * @return the array for r,g,b array histogram
	 */
	private static ArrayList<int[]> getHistogram(BufferedImage image) {
		// init our colour arrays
		int[] red, green, blue;
		red = new int[256];
		green = new int[256];
		blue = new int[256];
		for (int i = 0; i < 256; ++i) {
			red[i] = 0;
			green[i] = 0;
			blue[i] = 0;
		}

		for (int i = 0; i < image.getWidth(); ++i)
			for (int x = 0; x < image.getHeight(); ++x) {
				Color c = new Color(image.getRGB(i, x));

				++red[c.getRed()];
				++green[c.getGreen()];
				++blue[c.getBlue()];
			}

		ArrayList<int[]> out = new ArrayList<>();
		out.add(red);
		out.add(green);
		out.add(blue);

		return out;
	}

	private static ArrayList<int[]> getEqualizedHistogram(BufferedImage image) {
		// get the histogram
		ArrayList<int[]> histo = getHistogram(image);

		// init our colour arrays
		int[] red, green, blue;
		red = new int[256];
		green = new int[256];
		blue = new int[256];
		for (int i = 0; i < 256; ++i) {
			red[i] = 0;
			green[i] = 0;
			blue[i] = 0;
		}

		long sr, sg, sb;
		sr = 0;
		sg = 0;
		sb = 0;

		float scaleFactor = (float) (255f / (float) (image.getWidth() * (float) image
				.getHeight()));

		for (int i = 0; i < 256; ++i) {
			// red
			sr += histo.get(0)[i];
			int valr = (int) (sr * scaleFactor);
			red[i] = Math.min(valr, 255);

			// green
			sg += histo.get(1)[i];
			int valg = (int) (sg * scaleFactor);
			green[i] = Math.min(valg, 255);

			// blue
			sb += histo.get(2)[i];
			int valb = (int) (sb * scaleFactor);
			blue[i] = Math.min(valb, 255);
		}

		ArrayList<int[]> out = new ArrayList<>();
		out.add(red);
		out.add(green);
		out.add(blue);

		return out;
	}

	public static ImageHolder equalizeImage(ImageHolder input) {

		ArrayList<int[]> eqhisto = getEqualizedHistogram(input
				.getBufferedImage());

		BufferedImage newImage = new BufferedImage(input.getWidth(),
				input.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < newImage.getWidth(); ++x)
			for (int y = 0; y < newImage.getHeight(); ++y) {
				Color c = new Color(input.getBufferedImage().getRGB(x, y));

				Color newc = new Color(eqhisto.get(0)[c.getRed()],
						eqhisto.get(1)[c.getGreen()],
						eqhisto.get(2)[c.getBlue()], c.getAlpha());

				newImage.setRGB(x, y, newc.getRGB());
			}

		return new ImageHolder(newImage);
	}

}
