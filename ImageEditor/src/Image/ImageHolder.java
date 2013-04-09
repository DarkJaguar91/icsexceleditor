package Image;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageHolder {
	// globals
	protected BufferedImage image;
	
	public ImageHolder(BufferedImage image){
		this.image = image;
	}
	
	public ImageHolder(Image inImage){
		image = new BufferedImage(inImage.getWidth(null), inImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		image.getGraphics().drawImage(inImage, 0, 0, image.getWidth(), image.getHeight(), null);		
	}
	
	public Image getImage(){
		return image.getScaledInstance(image.getWidth(), image.getHeight(), BufferedImage.SCALE_SMOOTH);
	}
	
	public BufferedImage getBufferedImage(){
		return image;
	}
	
	public int getWidth(){
		return image.getWidth();
	}
	
	public int getHeight(){
		return image.getHeight();
	}
}
