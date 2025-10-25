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

public class ImageEncryptor {
    private static final byte[] SALT = "fixedsaltexample".getBytes(); // Fixed salt for demo (16 bytes)
    private static final int ITERATIONS = 65536; // High for security
    private static final int KEY_LENGTH_BITS = 384; // 256-bit key + 128-bit IV = 384 bits

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the secret key (password): ");
        String password = scanner.nextLine();
        scanner.close();

        try {
            // Load the original image from local disk (relative path in project root)
            File inputFile = new File("original.png");
            BufferedImage image = ImageIO.read(inputFile);

            // Derive AES key and IV from password
            byte[] keyAndIv = deriveKeyAndIv(password);
            byte[] key = new byte[32];
            byte[] iv = new byte[16];
            System.arraycopy(keyAndIv, 0, key, 0, 32);
            System.arraycopy(keyAndIv, 32, iv, 0, 16);

            // Encrypt the image pixel data
            encryptImage(image, key, iv);

            // Save the encrypted image to project root
            File outputFile = new File("encrypted.png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Encryption complete. Encrypted image saved as 'encrypted.png'.");
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static byte[] deriveKeyAndIv(String password) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, KEY_LENGTH_BITS);
        return factory.generateSecret(spec).getEncoded();
    }

    private static void encryptImage(BufferedImage image, byte[] key, byte[] iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

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

        // Encrypt the pixel data
        byte[] encryptedData = cipher.doFinal(pixelData);

        // Put back into image (preserve alpha)
        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                int red = encryptedData[index++] & 0xFF;
                int green = encryptedData[index++] & 0xFF;
                int blue = encryptedData[index++] & 0xFF;
                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRgb);
            }
        }
    }
}