package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class AccountIsActiveException extends BadRequestException {

  public AccountIsActiveException() {
    setCode("Bad Request");
    setMessage("Account is active");
  }
}