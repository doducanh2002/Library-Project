package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.NotFoundException;

public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException(String userId) {
    setCode("NotFound");
    addParams("user",userId);
  }
}