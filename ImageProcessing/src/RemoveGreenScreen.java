import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RemoveGreenScreen{
	/**
	 * Read a png image into a BufferedImage.
	 * If the image is missing an alpha channel, return an error. We want ARGB, not RGB.
	 * Turn the BufferedImage into an int[][] of pixel values.
	 * Convert every pixel in the int[][] into a red pixel.
	 * Write the int[][] out to a new png image.
	 */
	public static void main(String[] args){
		BufferedImage input = null;
		
		try {
			input = ImageIO.read(RemoveGreenScreen.class.getResource("resources/books_with_alpha.png"));
		} catch ( IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		if(input.getAlphaRaster() == null) {
			System.err.println("[Unsupported Image Format] Image doesn't have an alpha channel. Exiting.");
			return;
		}
		int[][] imageAsArray = UtilityFunctions.turnInto2DArray(input);
		
		turnGreenBookIntoBlackBook(imageAsArray);
		
		UtilityFunctions.writeImage(imageAsArray, "the_whole_thing_is_red.png");
	}
	
	
	/**
	 * Little function that takes a 2D array of ARGB pixel data and turns all the pixels red.
	 * 
	 * @param image
	 * @return void - the input is modified in place
	 * 
	 * @todo - play with the three CUTOFF values to make your green screen work.
	 */
	private static void turnGreenBookIntoBlackBook(int[][] inputArray){
		final int GREEN_CUTOFF =  250;
		final int RED_CUTOFF   =  34;
		final int BLUE_CUTOFF  =  175;
		
		for(int row = 0; row < inputArray.length; row++) {
			for(int col = 0; col < inputArray[row].length; col++) {
				int currentPixel = inputArray[row][col];
				int red =   (currentPixel & 0x00FF0000 ) >> 16;
				int green = (currentPixel & 0x0000FF00 ) >> 8;
				int blue =   currentPixel & 0x000000FF;
				if((green < GREEN_CUTOFF ) && ( red < RED_CUTOFF ) && ( blue < BLUE_CUTOFF )){
					inputArray[row][col] = 0xFF000000;
				}else if((green > GREEN_CUTOFF) && (red < RED_CUTOFF) && (blue > BLUE_CUTOFF)){
					inputArray[row][col] = 0xFF000000;
				}
			}
		}
	}
}
