package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.NotFoundException;

public class EmailNotFoundException extends NotFoundException {

  public EmailNotFoundException(String email) {
    setCode("NotFound");
    setMessage(String.format("Email %s not found", email));
    addParams("email", email);
  }
}