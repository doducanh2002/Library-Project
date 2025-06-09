package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.BadRequestException;

public class EmailAlreadyExistsException extends BadRequestException {

  public EmailAlreadyExistsException() {
    setCode("Bad Request");
    setMessage("Email already exists");
  }
}