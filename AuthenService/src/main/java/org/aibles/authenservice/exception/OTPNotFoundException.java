package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.NotFoundException;

public class OTPNotFoundException extends NotFoundException {

  public OTPNotFoundException(String username) {
    setMessage("OTP not found for username " + username);
  }
}