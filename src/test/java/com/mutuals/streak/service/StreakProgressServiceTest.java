package com.mutuals.streak.service;

import com.mutuals.config.AppProperties;
import com.mutuals.economy.service.WalletService;
import com.mutuals.event.StreakMilestoneReachedEvent;
import com.mutuals.social.entity.Mutual;
import com.mutuals.streak.entity.Streak;
import com.mutuals.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreakProgressServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);

    @Mock
    private PersonalStreakService personalStreakService;
    @Mock
    private WalletService walletService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StreakProgressService service;
    private User ana;
    private User jhan;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties("America/Lima", null, null,
                new AppProperties.Streak(4, 24, 10, List.of(7, 30)), null, null, null, null, null, null, null);
        service = new StreakProgressService(personalStreakService, walletService, properties, eventPublisher);
        ana = user(1L);
        jhan = user(2L);
    }

    @Test
    void countsFirstConfirmedInteractionOfTheDay() {
        Streak streak = streak(3, TODAY.minusDays(1));

        boolean counted = service.registerConfirmedDay(streak, TODAY);

        assertThat(counted).isTrue();
        assertThat(streak.getCurrentLength()).isEqualTo(4);
        assertThat(streak.getLastActiveDate()).isEqualTo(TODAY);
        verify(personalStreakService).registerActivity(ana, TODAY);
        verify(personalStreakService).registerActivity(jhan, TODAY);
    }

    @Test
    void ignoresSecondInteractionOnSameDay() {
        Streak streak = streak(4, TODAY);

        boolean counted = service.registerConfirmedDay(streak, TODAY);

        assertThat(counted).isFalse();
        assertThat(streak.getCurrentLength()).isEqualTo(4);
        verify(walletService, never()).earnGems(any(), anyInt(), anyString());
    }

    @Test
    void rewardsGemsAndPublishesMilestoneWhenCompletingAWeek() {
        Streak streak = streak(6, TODAY.minusDays(1));

        service.registerConfirmedDay(streak, TODAY);

        assertThat(streak.getWeeksRewarded()).isEqualTo(1);
        verify(walletService, times(2)).earnGems(any(User.class), eq(10), anyString());
        verify(eventPublisher).publishEvent(any(StreakMilestoneReachedEvent.class));
    }

    private Streak streak(int length, LocalDate lastActive) {
        Mutual mutual = new Mutual();
        mutual.setUserA(ana);
        mutual.setUserB(jhan);
        Streak streak = new Streak();
        streak.setMutual(mutual);
        streak.setCurrentLength(length);
        streak.setStartDate(TODAY.minusDays(length));
        streak.setLastActiveDate(lastActive);
        return streak;
    }

    private User user(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
