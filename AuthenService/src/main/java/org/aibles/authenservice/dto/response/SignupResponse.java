package org.aibles.authenservice.dto.response;

public class SignupResponse {

  private String email;
  private String username;

  public SignupResponse() {
  }

  public SignupResponse(String email, String username) {
    this.username = username;
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
