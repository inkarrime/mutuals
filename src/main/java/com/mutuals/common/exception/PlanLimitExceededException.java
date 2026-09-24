package com.mutuals.common.exception;

import java.util.Map;

public class PlanLimitExceededException extends ForbiddenOperationException {

    public PlanLimitExceededException(int limit) {
        super("Free plan allows up to " + limit + " active streaks. Upgrade to Mutuals Plus for unlimited streaks.",
                Map.of("limit", limit, "upgrade", "PLUS"));
    }
}
