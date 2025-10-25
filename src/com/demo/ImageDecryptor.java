package com.demo;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Scanner;

public class ImageDecryptor {
    private static final byte[] SALT = "fixedsaltexample".getBytes(); // Must match encryption salt
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 384;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the secret key (password): ");
        String password = scanner.nextLine();
        scanner.close();

        try {
            // Load the encrypted image from local disk (relative path in project root)
            File inputFile = new File("encrypted.png");
            BufferedImage image = ImageIO.read(inputFile);

            // Derive AES key and IV from password (must match encryption)
            byte[] keyAndIv = deriveKeyAndIv(password);
            byte[] key = new byte[32];
            byte[] iv = new byte[16];
            System.arraycopy(keyAndIv, 0, key, 0, 32);
            System.arraycopy(keyAndIv, 32, iv, 0, 16);

            // Decrypt the image pixel data
            decryptImage(image, key, iv);

            // Save the decrypted image to project root
            File outputFile = new File("decrypted.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Decryption complete. Decrypted image saved as 'decrypted.png'.");
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static byte[] deriveKeyAndIv(String password) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, KEY_LENGTH_BITS);
        return factory.generateSecret(spec).getEncoded();
    }

    private static void decryptImage(BufferedImage image, byte[] key, byte[] iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        int width = image.getWidth();
        int height = image.getHeight();
        byte[] pixelData = new byte[width * height * 3];
        int index = 0;

        // Extract RGB bytes
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                pixelData[index++] = (byte) ((rgb >> 16) & 0xFF); // Red
                pixelData[index++] = (byte) ((rgb >> 8) & 0xFF);  // Green
                pixelData[index++] = (byte) (rgb & 0xFF);        // Blue
            }
        }

        // Decrypt the pixel data
        byte[] decryptedData = cipher.doFinal(pixelData);

        // Put back into image (preserve alpha)
        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                int red = decryptedData[index++] & 0xFF;
                int green = decryptedData[index++] & 0xFF;
                int blue = decryptedData[index++] & 0xFF;
                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRgb);
            }
        }
    }
}