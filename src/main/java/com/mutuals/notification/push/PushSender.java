package com.mutuals.notification.push;

import java.util.List;

public interface PushSender {

    void send(List<String> deviceTokens, PushMessage message);
}
