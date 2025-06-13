package org.aibles.authenservice.exception;

import org.aibles.authenservice.exception.base.NotFoundException;

public class UsernameNotFoundException extends NotFoundException {

  public UsernameNotFoundException(String username) {
    setCode("NotFound");
    addParams("user", username);
  }
}