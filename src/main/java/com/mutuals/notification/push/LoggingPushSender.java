package com.mutuals.notification.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "mutuals.push.provider", havingValue = "log", matchIfMissing = true)
public class LoggingPushSender implements PushSender {

    @Override
    public void send(List<String> deviceTokens, PushMessage message) {
        log.info("[push] to {} device(s): {} - {}", deviceTokens.size(), message.title(), message.body());
    }
}
