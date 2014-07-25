package com.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil {
	public static byte[] getImageBytes(File file) {
		byte[] data = null;
		// load image data to byte array
        try {        	
        	BufferedImage bi = ImageIO.read(file);
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		ImageIO.write(bi, "png", out);
    		data = out.toByteArray();
    		out.close();
        } catch (IOException exception) {
        	exception.printStackTrace();
        }
		return data;
	}
}
