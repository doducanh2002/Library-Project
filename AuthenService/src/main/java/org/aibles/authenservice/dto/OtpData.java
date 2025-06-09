package org.aibles.authenservice.dto;

import java.io.Serializable;

public class OtpData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String otpCode;
    private boolean isUsed;
    
    public OtpData() {}
    
    public OtpData(String username, String otpCode) {
        this.username = username;
        this.otpCode = otpCode;
        this.isUsed = false;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public boolean isUsed() {
        return isUsed;
    }
    
    public void setUsed(boolean used) {
        isUsed = used;
    }
}