package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class AccountIsLockedException extends BadRequestException {

  public AccountIsLockedException() {
    setCode("Bad Request");
    setMessage("Account is locked");
  }
}