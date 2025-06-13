package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class OTPInvalidException extends BadRequestException {

  public OTPInvalidException() {
    setCode("Bad Request");
    setMessage("OTP Invalid");
  }
}