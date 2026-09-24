package com.mutuals.common.exception;

public class InteractionExpiredException extends InvalidOperationException {

    public InteractionExpiredException(Long interactionId) {
        super("Interaction " + interactionId + " is no longer pending");
    }
}
