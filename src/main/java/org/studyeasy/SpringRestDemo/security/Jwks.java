package org.studyeasy.SpringRestDemo.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.RSAKey;

public class Jwks {

    private Jwks() {
    }

    public static RSAKey generateRsa() {
        final var keyPair = KeyGeneratorUtils.generateRsaKey();
        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }
}
