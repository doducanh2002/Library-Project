package org.aibles.authenservice.exception.base;

public class BadRequestException extends RunException {

    public BadRequestException() {
        setStatus(400);
        setMessage("org.aibles.authenservice.exception.base.BadRequestException");
    }
}
