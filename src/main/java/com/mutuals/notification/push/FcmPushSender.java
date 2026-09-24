package com.mutuals.notification.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.mutuals.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "mutuals.push.provider", havingValue = "fcm")
public class FcmPushSender implements PushSender {

    private final FirebaseMessaging messaging;

    public FcmPushSender(AppProperties properties) throws IOException {
        try (InputStream credentials = new FileInputStream(properties.push().firebaseCredentialsPath())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
            this.messaging = FirebaseMessaging.getInstance(app);
        }
    }

    @Override
    public void send(List<String> deviceTokens, PushMessage message) {
        if (deviceTokens.isEmpty()) {
            return;
        }
        MulticastMessage multicast = MulticastMessage.builder()
                .addAllTokens(deviceTokens)
                .setNotification(Notification.builder().setTitle(message.title()).setBody(message.body()).build())
                .putAllData(message.data())
                .build();
        try {
            messaging.sendEachForMulticast(multicast);
        } catch (FirebaseMessagingException ex) {
            log.warn("FCM delivery failed: {}", ex.getMessage());
        }
    }
}
