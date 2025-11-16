package com.demo;

import java.nio.file.Path;
import java.security.KeyPair;

public class KeyGen {
    public static void main(String[] args) throws Exception {
        int bits = 2048;
        if (args.length >= 1) {
            bits = Integer.parseInt(args[0]);
        }
        KeyPair kp = CryptoUtils.generateRsaKeyPair(bits);
        CryptoUtils.savePublicKeyPem(kp.getPublic(), Path.of("public.pem"));
        CryptoUtils.savePrivateKeyPem(kp.getPrivate(), Path.of("private.pem"));
        System.out.println("Generated RSA keys: public.pem and private.pem (" + bits + " bits)");
    }
}
