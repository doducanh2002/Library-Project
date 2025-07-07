package com.library.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

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

      public List<String> extractRoles(String token) {
          try {
              String[] parts = token.split("\\.");
              String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
              
              System.out.println("JWT Payload: " + payload);

              ObjectMapper mapper = new ObjectMapper();
              JsonNode payloadNode = mapper.readTree(payload);
              
              List<String> roles = new ArrayList<>();
              JsonNode rolesNode = payloadNode.get("roles");
              
              System.out.println("Roles node from JWT: " + rolesNode);
              
              if (rolesNode != null && rolesNode.isArray()) {
                  for (JsonNode roleNode : rolesNode) {
                      roles.add(roleNode.asText());
                  }
              }
              
              System.out.println("Extracted roles: " + roles);
              return roles;
          } catch (Exception e) {
              System.out.println("Error extracting roles: " + e.getMessage());
              return new ArrayList<>();
          }
      }
  }
