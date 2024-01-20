package org.studyeasy.SpringRestDemo.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.springframework.stereotype.Component;

@Component
public final class KeyGeneratorUtils {

    private KeyGeneratorUtils() {
    }

    public static KeyPair generateRsaKey() {
        try {
            final var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
