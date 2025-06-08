package org.aibles.authenservice.utill;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class SecurityUtil {

  public static String getCredential() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return "SYSTEM_ID";
    }

    if (authentication.getPrincipal() instanceof String) {
      return (String) authentication.getPrincipal();
    } else if (authentication.getName() != null) {
      return authentication.getName();
    }

    return "SYSTEM_ID";
  }
}