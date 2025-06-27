package org.library.fileservice.autheb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.util.JwkClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;

@Component
  public class JwtVerifier {

      private final JwkClient jwkClient;

      public JwtVerifier(JwkClient jwkClient) {
          this.jwkClient = jwkClient;
      }

      public boolean validateToken(String token) {
          try {
              String[] parts = token.split("\\.");
              if (parts.length != 3) {
                  return false;
              }

              String dataToVerify = parts[0] + "." + parts[1];
              byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

              Signature verifier = Signature.getInstance("SHA256withRSA");
              verifier.initVerify(jwkClient.getPublicKey());
              verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));

              boolean isValidSignature = verifier.verify(signatureBytes);

              // Check expiration
              long exp = extractExpiration(token);
              boolean isNotExpired = exp > (System.currentTimeMillis() / 1000);

              return isValidSignature && isNotExpired;

          } catch (Exception e) {
              return false;
          }
      }

      public String extractUsername(String token) {
          try {
              String[] parts = token.split("\\.");
              String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

              ObjectMapper mapper = new ObjectMapper();
              JsonNode payloadNode = mapper.readTree(payload);
              return payloadNode.get("sub").asText();

          } catch (Exception e) {
              return null;
          }
      }

      private long extractExpiration(String token) {
          try {
              String[] parts = token.split("\\.");
              String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

              ObjectMapper mapper = new ObjectMapper();
              JsonNode payloadNode = mapper.readTree(payload);
              return payloadNode.get("exp").asLong();

          } catch (Exception e) {
              return 0;
          }
      }
  }
