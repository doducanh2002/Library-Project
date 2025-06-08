package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class PasswordConfirmNotMatchException extends BadRequestException {

  public PasswordConfirmNotMatchException() {
    setCode("Bad Request");
    setMessage("Password %password% and confirm password %confirmPassword% don't match");
  }
}