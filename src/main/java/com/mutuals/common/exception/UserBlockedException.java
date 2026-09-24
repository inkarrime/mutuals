package com.mutuals.common.exception;

public class UserBlockedException extends ForbiddenOperationException {

    public UserBlockedException() {
        super("This action is not available between these users");
    }
}
