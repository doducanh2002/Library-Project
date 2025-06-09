package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class PasswordSimilarException extends BadRequestException {

  public PasswordSimilarException() {
    setCode("Bad Request");
    setMessage("The old password and new password must not be the same");
  }
}