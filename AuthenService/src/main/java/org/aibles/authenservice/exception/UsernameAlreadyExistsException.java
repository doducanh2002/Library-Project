package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class UsernameAlreadyExistsException extends BadRequestException {

  public UsernameAlreadyExistsException() {
    setCode("Bad Request");
    setMessage("Username already exists");
  }
}