package com.mutuals.common.exception;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
