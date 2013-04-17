package GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import Image.ImageHolder;
import Image.ImageProcessor;

public class ImagePanel extends JPanel implements MouseListener,
		MouseMotionListener {

	/**
	 * Generated Serial ID
	 */
	private static final long serialVersionUID = 4201844286968346886L;

	// Globals
	private ImageHolder image = null;
	private Point[] points;

	public ImagePanel(ImageHolder image) {
		this.image = image;

		points = new Point[2]; // for drawing the box
		points[0] = null;
		points[1] = null;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@Override
	public void paint(Graphics g) {
		if (image != null) {
			super.paint(g);

			// draw image
			g.drawImage(image.getImage(), 0, 0, this.getWidth(),
					this.getHeight(), this);

			if (points[0] != null && points[1] != null) {
				g.setColor(Color.red);
				int x = Math.min(points[0].x, points[1].x);
				int y = Math.min(points[0].y, points[1].y);
				int width = Math.abs(points[0].x - points[1].x);
				int height = Math.abs(points[0].y - points[1].y);

				g.drawRect(x, y, width, height);
			}
		}
	}

	public Point convertToImageCoords(Point point) {
		Point imagePoint = new Point();

		imagePoint.x = (int) (point.x * (float) (image.getWidth() / (float) this
				.getWidth()));
		imagePoint.y = (int) (point.y * (float) (image.getHeight() / (float) this
				.getHeight()));
		return imagePoint;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	// Mouse methods
	// //////////////////////////////////////////////////////////////////////////////////////////////////

	// required mouse globals
	Point firstClick = null;

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!firstClick.equals(e.getPoint())) {
			points[0] = firstClick;
			points[1] = e.getPoint();
		}
		updateUI();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (points[0] != null) {
				int x = Math.min(points[0].x, points[1].x);
				int y = Math.min(points[0].y, points[1].y);
				int width = Math.abs(points[0].x - points[1].x);
				int height = Math.abs(points[0].y - points[1].y);
				Point start = new Point(x, y);
				Point end = new Point(x + width, y + height);

				start = convertToImageCoords(start);
				end = convertToImageCoords(end);
				image = ImageProcessor.equalizeImage(image, start, end);

			} else
				image = ImageProcessor.equalizeImage(image, null, null);

			updateUI();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			firstClick = e.getPoint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (!firstClick.equals(e.getPoint())) {
				points[0] = firstClick;
				points[1] = e.getPoint();
			}
			updateUI();
		}
	}
}
