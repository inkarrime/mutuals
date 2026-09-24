package com.mutuals.email;

import com.mutuals.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Locale SPANISH = Locale.forLanguageTag("es-PE");

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final AppProperties properties;

    public void send(String to, String subject, String template, Map<String, Object> variables) {
        if (!properties.mail().enabled()) {
            log.info("[mail disabled] {} -> {}", subject, to);
            return;
        }
        Context context = new Context(SPANISH);
        context.setVariables(variables);
        context.setVariable("frontendUrl", properties.mail().frontendUrl());
        context.setVariable("subject", subject);
        String html = templateEngine.process("email/" + template, context);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            log.warn("Could not send '{}' email to {}: {}", subject, to, ex.getMessage());
        }
    }
}
