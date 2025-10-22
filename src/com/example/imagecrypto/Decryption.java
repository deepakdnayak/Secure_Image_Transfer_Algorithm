package com.example.imagecrypto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Decryption {

    public static void main(String[] args) {
        try {
            // Step 1: Load the encrypted image
            File inputFile = new File("encrypted.png");
            BufferedImage encryptedImage = ImageIO.read(inputFile);
            System.out.println("Encrypted image loaded successfully.");

            // Step 2: Use the same key used for encryption
            int key = 123; // must match the encryption key

            // Step 3: Decrypt the image pixels
            int width = encryptedImage.getWidth();
            int height = encryptedImage.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = encryptedImage.getRGB(x, y);

                    // Extract color channels
                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;

                    // XOR again with the same key to decrypt
                    red = red ^ key;
                    green = green ^ key;
                    blue = blue ^ key;

                    int decryptedPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    encryptedImage.setRGB(x, y, decryptedPixel);
                }
            }

            // Step 4: Save the decrypted image
            File outputFile = new File("decrypted.png");
            ImageIO.write(encryptedImage, "png", outputFile);
            System.out.println("Image decrypted successfully! Decrypted image saved as 'decrypted.png'.");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
