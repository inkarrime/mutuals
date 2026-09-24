package com.mutuals.email;

import com.mutuals.config.AppProperties;
import com.mutuals.event.PasswordResetRequestedEvent;
import com.mutuals.event.SubscriptionActivatedEvent;
import com.mutuals.event.UserRegisteredEvent;
import com.mutuals.event.WrappedGeneratedEvent;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import com.mutuals.wrapped.repository.WrappedSummaryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final WrappedSummaryRepository wrappedSummaryRepository;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onRegistered(UserRegisteredEvent event) {
        userRepository.findById(event.userId()).ifPresent(user ->
                emailService.send(user.getEmail(), "Bienvenido a Mutuals", "welcome", Map.of("name", user.getDisplayName())));
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onPasswordReset(PasswordResetRequestedEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> {
            String resetUrl = properties.mail().frontendUrl() + "/reset-password?token="
                    + URLEncoder.encode(event.rawToken(), StandardCharsets.UTF_8);
            emailService.send(user.getEmail(), "Restablece tu contraseña", "password-reset",
                    Map.of("name", user.getDisplayName(), "resetUrl", resetUrl));
        });
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onWrapped(WrappedGeneratedEvent event) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null || !user.getPreferences().isEmailNotificationsEnabled()) {
            return;
        }
        wrappedSummaryRepository.findById(event.wrappedId()).ifPresent(summary -> {
            try {
                JsonNode stats = objectMapper.readTree(summary.getStatsJson());
                Map<String, Object> variables = new HashMap<>();
                variables.put("periodLabel", "Wrapped " + event.periodKey());
                variables.put("periodKey", event.periodKey());
                variables.put("interactions", stats.path("interactions").asLong());
                variables.put("activeDays", stats.path("activeDays").asLong());
                variables.put("longestStreak", stats.path("longestStreak").asInt());
                variables.put("topMutual", stats.path("topMutualDisplayName").isTextual()
                        ? stats.path("topMutualDisplayName").asText() : null);
                emailService.send(user.getEmail(), "Tu Wrapped de Mutuals está listo", "wrapped-ready", variables);
            } catch (Exception ex) {
                log.warn("Could not build wrapped email for user {}: {}", event.userId(), ex.getMessage());
            }
        });
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onSubscription(SubscriptionActivatedEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> emailService.send(user.getEmail(),
                "Bienvenido a Mutuals Plus", "subscription-activated",
                Map.of("name", user.getDisplayName(),
                        "expiresAt", DATE.format(event.expiresAt().atZone(ZoneId.of(properties.zone()))))));
    }
}
