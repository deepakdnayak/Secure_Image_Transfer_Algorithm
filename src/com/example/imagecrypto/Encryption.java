package com.example.imagecrypto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Encryption {

    public static void main(String[] args) {
        try {
            // Step 1: Load the original image
            File inputFile = new File("source.jpg");  // Place your image in project root
            BufferedImage image = ImageIO.read(inputFile);
            System.out.println("Original image loaded successfully.");

            // Step 2: Define a key for encryption (must be same for decryption)
            int key = 123; // you can choose any integer as key

            // Step 3: Encrypt the image pixels
            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    // XOR encryption for RGB channels
                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;

                    // XOR with key
                    red = red ^ key;
                    green = green ^ key;
                    blue = blue ^ key;

                    int encryptedPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, encryptedPixel);
                }
            }

            // Step 4: Save the encrypted image
            File outputFile = new File("encrypted.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image encrypted successfully! Encrypted image saved as 'encrypted.png'.");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
