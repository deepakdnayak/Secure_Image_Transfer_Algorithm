package com.example.imagecrypto;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class Main {

    // --- Image encryption logic ---
    public static class ImageEncryptor {

        // Generate a reproducible seed from key (sha-256 -> long)
        private static long seedFromKey(String key) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
                // compress first 8 bytes into a long
                long seed = 0L;
                for (int i = 0; i < 8 && i < hash.length; i++) {
                    seed = (seed << 8) | (hash[i] & 0xFF);
                }
                return seed;
            } catch (Exception e) {
                // fallback
                return key.hashCode();
            }
        }

        // Create a permutation of 0 to n-1 using Fisher-Yates and given seed
        private static int[] makePermutation(int n, long seed) {
            int[] perm = new int[n];
            for (int i = 0; i < n; i++) perm[i] = i;
            Random rnd = new Random(seed);
            for (int i = n - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int tmp = perm[i];
                perm[i] = perm[j];
                perm[j] = tmp;
            }
            return perm;
        }

        // Build inverse permutation
        private static int[] inversePermutation(int[] perm) {
            int n = perm.length;
            int[] inv = new int[n];
            for (int i = 0; i < n; i++) inv[perm[i]] = i;
            return inv;
        }

        // XOR byte sequence generator (cycle over key hash bytes)
        private static byte[] keyStream(String key, int length) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
                byte[] stream = new byte[length];
                for (int i = 0; i < length; i++) stream[i] = hash[i % hash.length];
                return stream;
            } catch (Exception e) {
                byte[] kb = key.getBytes(StandardCharsets.UTF_8);
                byte[] stream = new byte[length];
                for (int i = 0; i < length; i++) stream[i] = kb[i % kb.length];
                return stream;
            }
        }

        // Encrypt image (permutation then XOR)
        public static BufferedImage encrypt(BufferedImage src, String key) {
            int w = src.getWidth();
            int h = src.getHeight();
            int n = w * h;
            int[] pixels = new int[n];
            src.getRGB(0, 0, w, h, pixels, 0, w);

            long seed = seedFromKey(key);
            int[] perm = makePermutation(n, seed);

            // Step 1: permute pixels
            int[] permuted = new int[n];
            for (int i = 0; i < n; i++) {
                permuted[i] = pixels[perm[i]];
            }

            // Step 2: XOR RGB channels using key-stream bytes
            byte[] kstream = keyStream(key, n * 3); // 3 bytes per pixel (R,G,B)
            int[] encrypted = new int[n];
            for (int i = 0; i < n; i++) {
                int argb = permuted[i];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int r2 = (r ^ (kstream[i * 3] & 0xFF)) & 0xFF;
                int g2 = (g ^ (kstream[i * 3 + 1] & 0xFF)) & 0xFF;
                int b2 = (b ^ (kstream[i * 3 + 2] & 0xFF)) & 0xFF;

                encrypted[i] = (a << 24) | (r2 << 16) | (g2 << 8) | b2;
            }

            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            out.setRGB(0, 0, w, h, encrypted, 0, w);
            return out;
        }

        // Decrypt image (XOR then inverse permutation)
        public static BufferedImage decrypt(BufferedImage enc, String key) {
            int w = enc.getWidth();
            int h = enc.getHeight();
            int n = w * h;
            int[] pixels = new int[n];
            enc.getRGB(0, 0, w, h, pixels, 0, w);

            // XOR step (reverse of XOR is XOR with same stream)
            byte[] kstream = keyStream(key, n * 3);
            int[] afterXor = new int[n];
            for (int i = 0; i < n; i++) {
                int argb = pixels[i];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int r2 = (r ^ (kstream[i * 3] & 0xFF)) & 0xFF;
                int g2 = (g ^ (kstream[i * 3 + 1] & 0xFF)) & 0xFF;
                int b2 = (b ^ (kstream[i * 3 + 2] & 0xFF)) & 0xFF;

                afterXor[i] = (a << 24) | (r2 << 16) | (g2 << 8) | b2;
            }

            long seed = seedFromKey(key);
            int[] perm = makePermutation(n, seed);
            int[] inv = inversePermutation(perm);

            // Apply inverse permutation
            int[] recovered = new int[n];
            for (int i = 0; i < n; i++) {
                recovered[inv[i]] = afterXor[i];
            }

            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            out.setRGB(0, 0, w, h, recovered, 0, w);
            return out;
        }
    }

    // --- Simple GUI for demonstration ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageCryptoFrame().setVisible(true);
        });
    }

    public static class ImageCryptoFrame extends JFrame {
        private BufferedImage original;
        private BufferedImage encrypted;
        private BufferedImage decrypted;

        private final JLabel lblOriginal = new JLabel();
        private final JLabel lblEncrypted = new JLabel();
        private final JLabel lblDecrypted = new JLabel();

        private final JTextField txtKey = new JTextField("mysecretkey", 20);

        public ImageCryptoFrame() {
            setTitle("Image Encryption Demo");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1100, 700);
            setLayout(new BorderLayout());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(new JLabel("Key:"));
            top.add(txtKey);

            JButton btnLoad = new JButton("Load Source Image");
            JButton btnEncrypt = new JButton("Encrypt");
            JButton btnDecrypt = new JButton("Decrypt");
            top.add(btnLoad);
            top.add(btnEncrypt);
            top.add(btnDecrypt);

            add(top, BorderLayout.NORTH);

            JPanel imagesPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            imagesPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            lblOriginal.setHorizontalAlignment(JLabel.CENTER);
            lblEncrypted.setHorizontalAlignment(JLabel.CENTER);
            lblDecrypted.setHorizontalAlignment(JLabel.CENTER);

            imagesPanel.add(wrapWithTitle(lblOriginal, "Original"));
            imagesPanel.add(wrapWithTitle(lblEncrypted, "Encrypted"));
            imagesPanel.add(wrapWithTitle(lblDecrypted, "Decrypted"));

            add(imagesPanel, BorderLayout.CENTER);

            // Actions
            btnLoad.addActionListener(e -> {
                try {
                    // Try resource folder first, then file chooser
                    File resourceFile = new File("resources/source.jpg");
                    if (resourceFile.exists()) {
                        original = ImageIO.read(resourceFile);
                    } else {
                        // if not found, allow user to choose file
                        JFileChooser chooser = new JFileChooser();
                        int res = chooser.showOpenDialog(this);
                        if (res == JFileChooser.APPROVE_OPTION) {
                            File f = chooser.getSelectedFile();
                            original = ImageIO.read(f);
                        } else {
                            JOptionPane.showMessageDialog(this, "No image selected.");
                            return;
                        }
                    }
                    displayImage(lblOriginal, original);
                    lblEncrypted.setIcon(null);
                    lblDecrypted.setIcon(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load image: " + ex.getMessage());
                }
            });

            btnEncrypt.addActionListener(e -> {
                if (original == null) {
                    JOptionPane.showMessageDialog(this, "Load the source image first.");
                    return;
                }
                String key = txtKey.getText();
                encrypted = ImageEncryptor.encrypt(original, key);
                displayImage(lblEncrypted, encrypted);
                // optionally save encrypted image
                try {
                    ImageIO.write(encrypted, "png", new File("resources/encrypted.png"));
                } catch (Exception ex) { /* ignore */ }
            });

            btnDecrypt.addActionListener(e -> {
                if (encrypted == null) {
                    JOptionPane.showMessageDialog(this, "Encrypt first.");
                    return;
                }
                String key = txtKey.getText();
                decrypted = ImageEncryptor.decrypt(encrypted, key);
                displayImage(lblDecrypted, decrypted);
                // optionally save decrypted image
                try {
                    ImageIO.write(decrypted, "png", new File("resources/decrypted.png"));
                } catch (Exception ex) { /* ignore */ }
            });
        }

        private JPanel wrapWithTitle(JLabel imgLabel, String title) {
            JPanel p = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel(title, JLabel.CENTER);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            p.add(titleLabel, BorderLayout.NORTH);
            JScrollPane sp = new JScrollPane(imgLabel);
            p.add(sp, BorderLayout.CENTER);
            return p;
        }

        private void displayImage(JLabel label, BufferedImage img) {
            if (img == null) {
                label.setIcon(null);
                return;
            }
            // scale image to fit label area (maintain aspect)
            int maxW = 340;
            int maxH = 520;
            Image scaled = img.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        }
    }
}
