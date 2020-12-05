import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;

public class Steganography {
	
	//private static final String MESSAGE_TO_HIDE = "I am the cryptomaster!!";
	//private static final String MESSAGE_TO_HIDE = "Youve been pwned";
	private static final String MESSAGE_TO_HIDE = "This is top secret classified info";
	
	private static final String ORIGINAL_IMAGE = "resources/pubgGreenScreen.png";
	
	private static final String IMAGE_WITH_EMBEDDED_TEXT = "output/steganographyOutput.png";
	
	public static void hideMessage(String inputFileName, String outputFileName, String asciiMessage) throws UnsupportedEncodingException {
		BufferedImage input = null;
		
		try {
			input = ImageIO.read(RemoveGreenScreen.class.getResource(inputFileName));
		} catch ( IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		if(input.getAlphaRaster() == null) {
			System.err.println("[Unsupported Image Format] Image doesn't have an alpha channel. Exiting.");
			return;
		}
		int[][] imageAsArray = UtilityFunctions.turnInto2DArray(input);

		byte[] asciiBytes= asciiMessage.getBytes("US-ASCII");
		
		final int numRows = imageAsArray.length;
		final int numCols = imageAsArray[0].length;
		
		for(int row = 0; row < numRows; row++) {
			for( int col = 0; col < numCols; col++) {
				if(col+row*numCols >= asciiBytes.length) {
					break;
				}
				hideByte( imageAsArray, row, col, asciiBytes[col + row*numCols]);
			}
		}
		
		UtilityFunctions.writeImage(imageAsArray, outputFileName);
	}
	
	private static void hideByte(int[][] imageAsArray, int row, int col, byte data) {
		// Note 0xFCFCFCFC equals
		// 11111100 11111100 11111100 11111100
		// so when we AND with 0xFCFCFCFC, we are setting the last two bits to zero.
		imageAsArray[row][col] &= 0xFCFCFCFC; // zero last 2 bits of every channel
		System.out.printf("0x%08X\n", imageAsArray[row][col]);
		int  aData = ((data >> 6) & 0x03) << 24;
		int  rData = ((data >> 4) & 0x03) << 16;
		int  gData = ((data >> 2) & 0x03) << 8;
		int  bData = ((data >> 0) & 0x03) << 0;
		System.out.printf("0x%08X, 0x%08X, 0x%08X, 0x%08X\n", aData, rData, gData, bData);
		imageAsArray[row][col] |= aData;
		imageAsArray[row][col] |= rData;
		imageAsArray[row][col] |= gData;
		imageAsArray[row][col] |= bData;
		System.out.printf("0x%08X\n\n", imageAsArray[row][col]);
	}
	
	public static void extractMessage(String inputFileName, int numBytesToExtract) throws UnsupportedEncodingException{
		BufferedImage input = null;
		
		try {
			input = ImageIO.read(new File(inputFileName));
		} catch ( IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		
		if(input.getAlphaRaster() == null) {
			System.err.println("[Unsupported Image Format] Image doesn't have an alpha channel. Exiting.");
			return;
		}
		int[][] imageAsArray = UtilityFunctions.turnInto2DArray(input);
		
		byte[] byteArr = new byte[numBytesToExtract];
		
		final int numRows = imageAsArray.length;
		final int numCols = imageAsArray[0].length;
		
		for(int row = 0; row < numRows; row++) {
			for( int col = 0; col < numCols; col++) {
				if(col+row*numCols >= numBytesToExtract) {
					break;
				}
				extractByte( imageAsArray, row, col, byteArr, col + row*numCols);
			}
		}

		System.out.println("Extracted message: " + new String(byteArr, "US-ASCII"));
	}
	
	/*
	 * extract a byte from imageArray[row][col] into byteArray[byteArrIndex]
	 * 
	 * @param imageArray - an array of ARGB pixel values containing some cryptographic data
	 * @param row - the row from imageArray we want to extract data from
	 * @param col - the col from imageArray we want to extract data from
	 * @param byteArray - our output array. Store the data we get from imageArray[row][col] in byteArray
	 * @byteArrIndex - the index in byteArray where we will put the data we extracted.
	 * 
	 * The data is hidden in the pixel at imageArray[row][col] like this:
	 * 
	 * binary pixel value: XXXXXXab XXXXXXcd XXXXXXef XXXXXXgh
	 * 
	 * The data needs to be extracted to form a new byte with these bits:
	 * 
	 * abcdefgh
	 * 
	 * then you say:
	 * byteArray[byteArrIndex] = abcdefgh;
	 * 
	 * and you're done.
	 */
	private static void extractByte(int[][] imageArray, int row, int col, byte[] byteArray, int byteArrIndex) {
		
		int currentpixel = imageArray[row][col];
		
		int tmp = imageArray[row][col];
		int aData = (tmp >> 18) & 0x00000C0;
	    int rData = (tmp >> 12) & 0x0000030;
		int gData = (tmp >> 6)  & 0x000000C;
		int bData = (tmp >> 0)  & 0x0000003;
		byteArray[byteArrIndex] = (byte)(aData |rData | gData |bData );}
		
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		hideMessage(ORIGINAL_IMAGE, IMAGE_WITH_EMBEDDED_TEXT, MESSAGE_TO_HIDE);
		extractMessage(IMAGE_WITH_EMBEDDED_TEXT, MESSAGE_TO_HIDE.length());
	}

}
