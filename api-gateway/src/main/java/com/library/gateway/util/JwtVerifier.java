package com.library.gateway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);
    private final JwkClient jwkClient;

    public JwtVerifier(JwkClient jwkClient) {
        this.jwkClient = jwkClient;
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }

        try {
            log.debug("Validating JWT token");
            
            // Split token into parts
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT format - expected 3 parts, got {}", parts.length);
                return false;
            }

            // Validate header
            if (!isValidHeader(parts[0])) {
                log.warn("Invalid JWT header");
                return false;
            }

            // Check expiration first (faster check)
            if (isTokenExpired(token)) {
                log.warn("JWT token is expired");
                return false;
            }

            // Validate signature
            String dataToVerify = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

            RSAPublicKey publicKey = jwkClient.getPublicKey();
            if (publicKey == null) {
                log.error("Unable to retrieve RSA public key for JWT verification");
                return false;
            }

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));

            boolean isValidSignature = verifier.verify(signatureBytes);
            
            if (isValidSignature) {
                log.debug("JWT token validation successful");
            } else {
                log.warn("JWT signature verification failed");
            }

            return isValidSignature;

        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Cannot extract username - invalid token format");
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadNode = mapper.readTree(payload);
            
            JsonNode subNode = payloadNode.get("sub");
            if (subNode == null) {
                log.warn("No 'sub' claim found in JWT token");
                return null;
            }

            String username = subNode.asText();
            log.debug("Extracted username from JWT: {}", username);
            return username;

        } catch (Exception e) {
            log.error("Failed to extract username from JWT token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            long exp = extractExpiration(token);
            if (exp == 0) {
                log.warn("No expiration claim found in token");
                return true;
            }

            long currentTime = Instant.now().getEpochSecond();
            boolean expired = exp <= currentTime;
            
            if (expired) {
                log.debug("Token expired. Expiry: {}, Current: {}", exp, currentTime);
            }
            
            return expired;

        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true; // Assume expired if we can't check
        }
    }

    private long extractExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadNode = mapper.readTree(payload);
            
            JsonNode expNode = payloadNode.get("exp");
            if (expNode == null) {
                return 0;
            }
            
            return expNode.asLong();

        } catch (Exception e) {
            log.error("Failed to extract expiration from token: {}", e.getMessage());
            return 0;
        }
    }

    private boolean isValidHeader(String headerPart) {
        try {
            String header = new String(Base64.getUrlDecoder().decode(headerPart), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(header);
            
            JsonNode algNode = headerNode.get("alg");
            JsonNode typNode = headerNode.get("typ");
            
            if (algNode == null || typNode == null) {
                return false;
            }
            
            String algorithm = algNode.asText();
            String type = typNode.asText();
            
            // Check for expected values
            return "RS256".equals(algorithm) && "JWT".equals(type);
            
        } catch (Exception e) {
            log.error("Failed to validate JWT header: {}", e.getMessage());
            return false;
        }
    }

    public JsonNode extractAllClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(payload);

        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }
}
