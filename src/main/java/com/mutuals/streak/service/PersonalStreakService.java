package com.mutuals.streak.service;

import com.mutuals.event.PersonalStreakUpdatedEvent;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PersonalStreakService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void registerActivity(User user, LocalDate date) {
        LocalDate last = user.getPersonalStreakLastDate();
        if (date.equals(last)) {
            return;
        }
        boolean continues = last != null && last.plusDays(1).equals(date);
        int value = continues ? user.getPersonalStreak() + 1 : 1;
        user.setPersonalStreak(value);
        user.setPersonalStreakLastDate(date);
        user.setLongestPersonalStreak(Math.max(user.getLongestPersonalStreak(), value));
        eventPublisher.publishEvent(new PersonalStreakUpdatedEvent(user.getId(), value));
    }
}
