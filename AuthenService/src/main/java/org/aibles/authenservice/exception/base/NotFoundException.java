package org.aibles.authenservice.exception.base;

public class NotFoundException extends RunException {

    public NotFoundException() {
        setStatus(404);
        setMessage("org.aibles.authenservice.exception.base.NotFoundException");
    }
}
