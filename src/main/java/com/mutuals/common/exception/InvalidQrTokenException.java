package com.mutuals.common.exception;

public class InvalidQrTokenException extends InvalidOperationException {

    public InvalidQrTokenException() {
        super("QR code is invalid or expired");
    }
}
