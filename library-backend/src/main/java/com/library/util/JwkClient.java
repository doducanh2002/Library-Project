package com.library.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Logger;

@Component
  public class JwkClient {

//      private static final Logger log = LoggerFactory.getLogger(JwkClient.class);

      @Value("${auth.service.jwk.url:http://localhost:8080/api/v1/auth/jwk/token}")
      private String jwkUrl;

      private final WebClient webClient;
      private RSAPublicKey cachedPublicKey;
      private long lastFetchTime = 0;
      private static final long CACHE_DURATION = 300000; // 5 minutes

      public JwkClient() {
          this.webClient = WebClient.builder()
              .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
              .build();
      }

      public RSAPublicKey getPublicKey() {
          long currentTime = System.currentTimeMillis();

          if (cachedPublicKey == null || (currentTime - lastFetchTime) > CACHE_DURATION) {
              try {
                  fetchPublicKey();
                  lastFetchTime = currentTime;
              } catch (Exception e) {
                  if (cachedPublicKey == null) {
                      throw new RuntimeException("No public key available for JWT verification", e);
                  }
              }
          }

          return cachedPublicKey;
      }

      private void fetchPublicKey() throws Exception {

          String jwkResponse = webClient.get()
              .uri(jwkUrl)
              .retrieve()
              .bodyToMono(String.class)
              .timeout(Duration.ofSeconds(10))
              .block();

          ObjectMapper mapper = new ObjectMapper();
          JsonNode jwkSet = mapper.readTree(jwkResponse);
          JsonNode firstKey = jwkSet.get("keys").get(0);

          String modulus = firstKey.get("n").asText();
          String exponent = firstKey.get("e").asText();

          byte[] nBytes = Base64.getUrlDecoder().decode(modulus);
          byte[] eBytes = Base64.getUrlDecoder().decode(exponent);

          BigInteger n = new BigInteger(1, nBytes);
          BigInteger e = new BigInteger(1, eBytes);

          RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
          KeyFactory factory = KeyFactory.getInstance("RSA");
          cachedPublicKey = (RSAPublicKey) factory.generatePublic(spec);

//          log.info("Successfully fetched and cached public key");
      }
  }
