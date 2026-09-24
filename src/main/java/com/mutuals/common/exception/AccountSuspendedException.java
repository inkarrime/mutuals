package com.mutuals.common.exception;

public class AccountSuspendedException extends ForbiddenOperationException {

    public AccountSuspendedException() {
        super("This account is suspended");
    }
}
