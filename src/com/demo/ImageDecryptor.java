package com.demo;

import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageDecryptor {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java com.demo.ImageDecryptor <encryptedImage.png> <privatePem> <wrappedAes.key> <wrappedHmac.key> [iv.bin] [hmac.bin]");
            System.exit(1);
        }

        Path encImage = Path.of(args[0]);
        Path privatePem = Path.of(args[1]);
        Path wrappedAes = Path.of(args[2]);
        Path wrappedHmac = Path.of(args[3]);
        Path ivPath = args.length >= 5 ? Path.of(args[4]) : Path.of("iv.bin");
        Path hmacPath = args.length >= 6 ? Path.of(args[5]) : Path.of("hmac.bin");

        try {
            // Load encrypted image
            BufferedImage image = ImageIO.read(encImage.toFile());
            int width = image.getWidth();
            int height = image.getHeight();

            // Extract encrypted RGB bytes
            byte[] encryptedPixels = new byte[width * height * 3];
            int idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    encryptedPixels[idx++] = (byte) ((rgb >> 16) & 0xFF);
                    encryptedPixels[idx++] = (byte) ((rgb >> 8) & 0xFF);
                    encryptedPixels[idx++] = (byte) (rgb & 0xFF);
                }
            }

            // Load wrapped keys and iv and hmac
            byte[] wrappedAesBytes = Files.readAllBytes(wrappedAes);
            byte[] wrappedHmacBytes = Files.readAllBytes(wrappedHmac);
            byte[] iv = Files.readAllBytes(ivPath);
            byte[] hmacSaved = Files.readAllBytes(hmacPath);

            // Load RSA private key and unwrap keys
            var rsaPrivate = CryptoUtils.loadPrivateKeyPem(privatePem);
            SecretKey aesKey = CryptoUtils.unwrapAesKey(wrappedAesBytes, rsaPrivate);
            SecretKey hmacKey = CryptoUtils.unwrapHmacKey(wrappedHmacBytes, rsaPrivate);

            // Verify HMAC
            byte[] computed = CryptoUtils.computeHmac(hmacKey, encryptedPixels);
            if (!java.util.Arrays.equals(computed, hmacSaved)) {
                System.err.println("HMAC verification failed! Data may be tampered or wrong key.");
                System.exit(2);
            }
            System.out.println("HMAC verified OK.");

            // Decrypt AES/CTR
            byte[] decryptedPixels = CryptoUtils.aesCtrDoFinal(aesKey, iv, javax.crypto.Cipher.DECRYPT_MODE, encryptedPixels);

            // Put decrypted pixels back into image (preserve alpha)
            idx = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
                    int r = decryptedPixels[idx++] & 0xFF;
                    int g = decryptedPixels[idx++] & 0xFF;
                    int b = decryptedPixels[idx++] & 0xFF;
                    int newRgb = (alpha << 24) | (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, newRgb);
                }
            }

            // Save decrypted file
            Path out = Path.of("decrypted.png");
            ImageIO.write(image, "png", out.toFile());
            System.out.println("Decryption complete. Saved decrypted image: " + out.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Decryption failed: " + e.getMessage());
        }
    }
}
