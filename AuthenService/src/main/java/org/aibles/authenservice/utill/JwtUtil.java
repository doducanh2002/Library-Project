package org.aibles.authenservice.utill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final KeyPair keyPair;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 phút
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 ngày

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public JwtUtil() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        this.keyPair = keyPairGenerator.generateKeyPair();
    }

    public String generateAccessToken(String username) {
        return createToken(username, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String username) {
        String refreshToken = createToken(username, REFRESH_TOKEN_EXPIRATION);
        redisTemplate.opsForValue().set("refresh_token:" + refreshToken, username, REFRESH_TOKEN_EXPIRATION / 1000, TimeUnit.SECONDS);
        return refreshToken;
    }

    private String createToken(String username, long expiration) {
        try {
            String header = "{\"alg\":\"RS256\",\"kid\":\"" + UUID.randomUUID().toString() + "\"}";
            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8));

            long now = System.currentTimeMillis();
            String payload = "{\"sub\":\"" + username + "\",\"iat\":" + (now / 1000) + ",\"exp\":" + ((now + expiration) / 1000) + "}";
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

            String dataToSign = encodedHeader + "." + encodedPayload;
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());

            return dataToSign + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String signature = parts[2];

            String dataToVerify = parts[0] + "." + parts[1];
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(keyPair.getPublic());
            verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));
            boolean isValidSignature = verifier.verify(Base64.getUrlDecoder().decode(signature));

            String extractedUsername = extractUsername(token);
            long exp = extractExpiration(token);
            boolean isNotExpired = exp > (System.currentTimeMillis() / 1000);

            return isValidSignature && extractedUsername.equals(username) && isNotExpired;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        String username = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        return username != null && validateToken(refreshToken, username);
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return payload.split("\"sub\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            return null;
        }
    }

    private long extractExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String expStr = payload.split("\"exp\":")[1].split("[,}]")[0];
            return Long.parseLong(expStr);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getJwk() {
        try {
            String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());
            String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(((RSAPublicKey) keyPair.getPublic()).getPublicExponent().toByteArray());
            return "{\"kty\":\"RSA\",\"n\":\"" + modulus + "\",\"e\":\"" + exponent + "\",\"alg\":\"RS256\",\"use\":\"sig\"}";
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWK", e);
        }
    }
}