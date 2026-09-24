package com.mutuals.wrapped.dto;

import java.util.List;

public record AllTimeWrappedResponse(
        UserWrappedStats overall,
        boolean fullAccess,
        List<FriendWrappedSection> friends
) {
}
