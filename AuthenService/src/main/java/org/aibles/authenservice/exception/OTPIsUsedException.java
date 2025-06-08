package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class OTPIsUsedException extends BadRequestException {

  public OTPIsUsedException() {
    setCode("Bad Request");
    setMessage("OTP is used");
  }
}