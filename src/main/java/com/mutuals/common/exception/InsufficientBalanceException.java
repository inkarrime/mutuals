package com.mutuals.common.exception;

import java.util.Map;

public class InsufficientBalanceException extends ConflictException {

    public InsufficientBalanceException(String currency, int required, int available) {
        super("Insufficient " + currency + " balance",
                Map.of("currency", currency, "required", required, "available", available));
    }
}
