package com.endava.personal.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int TOKEN_BYTES = 48;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();

    public String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return urlEncoder.encodeToString(bytes);
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return urlEncoder.encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
