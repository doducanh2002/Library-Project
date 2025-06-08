package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class PasswordInvalidException extends BadRequestException {

  public PasswordInvalidException() {
    setCode("BadRequest");
    setMessage("Password is incorrect");
  }
}