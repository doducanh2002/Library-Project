package com.library.gateway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;

@Component
public class JwkClient {

    private static final Logger log = LoggerFactory.getLogger(JwkClient.class);

    @Value("${jwt.auth-service.jwk-url}")
    private String jwkUrl;

    private final WebClient webClient;
    private RSAPublicKey cachedPublicKey;
    private long lastFetchTime = 0;
    private static final long CACHE_DURATION = 300000; // 5 minutes
    private static final int MAX_RETRY_ATTEMPTS = 3;

    public JwkClient() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    public RSAPublicKey getPublicKey() {
        long currentTime = System.currentTimeMillis();

        // Check if we need to refresh the key
        if (cachedPublicKey == null || (currentTime - lastFetchTime) > CACHE_DURATION) {
            log.debug("Public key cache expired or empty, fetching new key from: {}", jwkUrl);
            
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    fetchPublicKey();
                    lastFetchTime = currentTime;
                    log.info("Successfully fetched and cached RSA public key (attempt {})", attempt);
                    break;
                    
                } catch (Exception e) {
                    log.warn("Failed to fetch public key (attempt {}): {}", attempt, e.getMessage());
                    
                    if (attempt == MAX_RETRY_ATTEMPTS) {
                        if (cachedPublicKey == null) {
                            log.error("No public key available for JWT verification after {} attempts", MAX_RETRY_ATTEMPTS);
                            throw new RuntimeException("No public key available for JWT verification", e);
                        } else {
                            log.warn("Using cached public key due to fetch failure");
                        }
                    } else {
                        // Wait before retry
                        try {
                            Thread.sleep(1000 * attempt); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        return cachedPublicKey;
    }

    private void fetchPublicKey() throws Exception {
        if (jwkUrl == null || jwkUrl.trim().isEmpty()) {
            throw new IllegalStateException("JWK URL is not configured");
        }

        try {
            log.debug("Fetching JWK from: {}", jwkUrl);
            
            String jwkResponse = webClient.get()
                .uri(jwkUrl)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            if (jwkResponse == null || jwkResponse.trim().isEmpty()) {
                throw new Exception("Empty response from JWK endpoint");
            }

            log.debug("Received JWK response: {}", jwkResponse);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jwkSet = mapper.readTree(jwkResponse);
            
            JsonNode keysNode = jwkSet.get("keys");
            if (keysNode == null || !keysNode.isArray() || keysNode.size() == 0) {
                throw new Exception("No keys found in JWK response");
            }

            JsonNode firstKey = keysNode.get(0);
            if (firstKey == null) {
                throw new Exception("First key in JWK set is null");
            }

            // Validate key type
            JsonNode ktyNode = firstKey.get("kty");
            if (ktyNode == null || !"RSA".equals(ktyNode.asText())) {
                throw new Exception("Expected RSA key type, got: " + (ktyNode != null ? ktyNode.asText() : "null"));
            }

            // Validate algorithm
            JsonNode algNode = firstKey.get("alg");
            if (algNode == null || !"RS256".equals(algNode.asText())) {
                throw new Exception("Expected RS256 algorithm, got: " + (algNode != null ? algNode.asText() : "null"));
            }

            JsonNode modulusNode = firstKey.get("n");
            JsonNode exponentNode = firstKey.get("e");
            
            if (modulusNode == null || exponentNode == null) {
                throw new Exception("Missing modulus (n) or exponent (e) in JWK");
            }

            String modulus = modulusNode.asText();
            String exponent = exponentNode.asText();

            if (modulus.isEmpty() || exponent.isEmpty()) {
                throw new Exception("Empty modulus or exponent in JWK");
            }

            byte[] nBytes = Base64.getUrlDecoder().decode(modulus);
            byte[] eBytes = Base64.getUrlDecoder().decode(exponent);

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey newPublicKey = (RSAPublicKey) factory.generatePublic(spec);

            // Validate the key
            if (newPublicKey.getModulus() == null || newPublicKey.getPublicExponent() == null) {
                throw new Exception("Generated RSA public key is invalid");
            }

            cachedPublicKey = newPublicKey;
            log.info("Successfully parsed and cached RSA public key");

        } catch (WebClientException e) {
            throw new Exception("Failed to fetch JWK from endpoint: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new Exception("Failed to parse JWK response: " + e.getMessage(), e);
        }
    }

    // For debugging/monitoring purposes
    public boolean isPublicKeyAvailable() {
        return cachedPublicKey != null;
    }

    public long getLastFetchTime() {
        return lastFetchTime;
    }

    public String getJwkUrl() {
        return jwkUrl;
    }
}
