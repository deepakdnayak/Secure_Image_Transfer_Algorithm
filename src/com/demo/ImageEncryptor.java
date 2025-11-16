package com.demo;

import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class ImageEncryptor {
    private static final int AES_KEY_SIZE = 256; // bits
    private static final int HMAC_KEY_SIZE = 256; // bits
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java com.demo.ImageEncryptor <inputImage.png> <rsaPublicPem>");
            System.exit(1);
        }
        Path inputImage = Path.of(args[0]);
        Path rsaPub = Path.of(args[1]);

        try {
            BufferedImage image = ImageIO.read(inputImage.toFile());
            int width = image.getWidth();
            int height = image.getHeight();

            // Extract pixel bytes (RGB)
            byte[] pixelData = new byte[width * height * 3];
            int idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    pixelData[idx++] = (byte) ((rgb >> 16) & 0xFF);
                    pixelData[idx++] = (byte) ((rgb >> 8) & 0xFF);
                    pixelData[idx++] = (byte) (rgb & 0xFF);
                }
            }

            // Generate AES key + IV and HMAC key
            SecretKey aesKey = CryptoUtils.generateAesKey(AES_KEY_SIZE);
            SecretKey hmacKey = CryptoUtils.generateHmacKey(HMAC_KEY_SIZE);
            byte[] iv = new byte[16];
            RANDOM.nextBytes(iv);

            // Encrypt pixelData with AES/CTR
            byte[] encryptedPixels = CryptoUtils.aesCtrDoFinal(aesKey, iv, javax.crypto.Cipher.ENCRYPT_MODE, pixelData);

            // Put encrypted pixels into image (preserve alpha)
            idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                    int r = encryptedPixels[idx++] & 0xFF;
                    int g = encryptedPixels[idx++] & 0xFF;
                    int b = encryptedPixels[idx++] & 0xFF;
                    int newRgb = (alpha << 24) | (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, newRgb);
                }
            }

            // Save encrypted image
            Path outImage = Path.of("encrypted.png");
            ImageIO.write(image, "png", outImage.toFile());
            System.out.println("Encrypted image written: " + outImage.toAbsolutePath());

            // Compute HMAC of encrypted pixels
            byte[] hmac = CryptoUtils.computeHmac(hmacKey, encryptedPixels);

            // Load RSA public key and wrap AES & HMAC keys
            var rsaPublic = CryptoUtils.loadPublicKeyPem(rsaPub);
            byte[] wrappedAes = CryptoUtils.wrapKey(aesKey, rsaPublic);
            byte[] wrappedHmac = CryptoUtils.wrapKey(hmacKey, rsaPublic);

            // Write companion files
            Files.write(Path.of("wrapped_aes.key"), wrappedAes);
            Files.write(Path.of("wrapped_hmac.key"), wrappedHmac);
            Files.write(Path.of("iv.bin"), iv);
            Files.write(Path.of("hmac.bin"), hmac);

            System.out.println("Wrote companion files: wrapped_aes.key, wrapped_hmac.key, iv.bin, hmac.bin");
            System.out.println("Now transfer these files together with encrypted.png to recipient.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Encryption failed: " + e.getMessage());
        }
    }
}
