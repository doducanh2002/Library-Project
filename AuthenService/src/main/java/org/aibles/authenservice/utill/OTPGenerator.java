package org.aibles.authenservice.utill;

import java.security.SecureRandom;
import java.text.DecimalFormat;

public class OTPGenerator {

  private static final String SIX_DIGITS_STRING = "000000";
  private static final Integer SIX_DIGITS_UPPER_BOUND = 1000000;

  public static String generateOtp() {
    SecureRandom random = new SecureRandom();
    return new DecimalFormat(SIX_DIGITS_STRING).format(random.nextInt(SIX_DIGITS_UPPER_BOUND));
  }
}
