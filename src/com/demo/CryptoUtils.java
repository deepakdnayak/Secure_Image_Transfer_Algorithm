package com.demo;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CryptoUtils {
    public static final String RSA_OAEP = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String AES_CTR_NOPAD = "AES/CTR/NoPadding";
    public static final String HMAC_SHA256 = "HmacSHA256";

    // RSA key pair generation
    public static KeyPair generateRsaKeyPair(int bits) throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(bits);
        return kpg.generateKeyPair();
    }

    // Save RSA public key as PEM (X.509)
    public static void savePublicKeyPem(PublicKey publicKey, Path out) throws IOException {
        byte[] encoded = publicKey.getEncoded();
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + chunk(Base64.getEncoder().encodeToString(encoded))
                + "-----END PUBLIC KEY-----\n";
        Files.write(out, pem.getBytes());
    }

    // Save RSA private key as PEM (PKCS#8)
    public static void savePrivateKeyPem(PrivateKey privateKey, Path out) throws IOException {
        byte[] encoded = privateKey.getEncoded();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + chunk(Base64.getEncoder().encodeToString(encoded))
                + "-----END PRIVATE KEY-----\n";
        Files.write(out, pem.getBytes());
    }

    // Load public key from PEM
    public static PublicKey loadPublicKeyPem(Path pemPath) throws GeneralSecurityException, IOException {
        String pem = Files.readString(pemPath);
        String b64 = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(b64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    // Load private key from PEM
    public static PrivateKey loadPrivateKeyPem(Path pemPath) throws GeneralSecurityException, IOException {
        String pem = Files.readString(pemPath);
        String b64 = pem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(b64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    // Generate random AES key
    public static SecretKey generateAesKey(int bits) throws GeneralSecurityException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(bits);
        return kg.generateKey();
    }

    // Generate random HMAC key (for HmacSHA256)
    public static SecretKey generateHmacKey(int bits) throws GeneralSecurityException {
        KeyGenerator kg = KeyGenerator.getInstance(HMAC_SHA256);
        // Usually HMAC keys can be 256 bits; some providers won't accept algorithm name here
        kg.init(bits);
        return kg.generateKey();
    }

    // Wrap a SecretKey using RSA public key (OAEP)
    public static byte[] wrapKey(SecretKey keyToWrap, PublicKey rsaPublic) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_OAEP);
        cipher.init(Cipher.WRAP_MODE, rsaPublic);
        return cipher.wrap(keyToWrap);
    }

    // Unwrap a wrapped key using RSA private key
    public static SecretKey unwrapAesKey(byte[] wrapped, PrivateKey rsaPrivate) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_OAEP);
        cipher.init(Cipher.UNWRAP_MODE, rsaPrivate);
        Key k = cipher.unwrap(wrapped, "AES", Cipher.SECRET_KEY);
        return new SecretKeySpec(k.getEncoded(), "AES");
    }

    public static SecretKey unwrapHmacKey(byte[] wrapped, PrivateKey rsaPrivate) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(RSA_OAEP);
        cipher.init(Cipher.UNWRAP_MODE, rsaPrivate);
        Key k = cipher.unwrap(wrapped, HMAC_SHA256, Cipher.SECRET_KEY);
        return new SecretKeySpec(k.getEncoded(), HMAC_SHA256);
    }

    // Compute HMAC-SHA256
    public static byte[] computeHmac(SecretKey hmacKey, byte[] data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(hmacKey);
        return mac.doFinal(data);
    }

    // AES-CTR encrypt/decrypt (same method; just init mode)
    public static byte[] aesCtrDoFinal(SecretKey aesKey, byte[] iv, int cipherMode, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AES_CTR_NOPAD);
        cipher.init(cipherMode, aesKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    // Helpers
    private static String chunk(String base64) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < base64.length()) {
            int end = Math.min(i + 64, base64.length());
            sb.append(base64, i, end).append("\n");
            i = end;
        }
        return sb.toString();
    }
}
