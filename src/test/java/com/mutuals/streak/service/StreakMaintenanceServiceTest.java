package com.mutuals.streak.service;

import com.mutuals.event.ShieldUsedEvent;
import com.mutuals.social.entity.Mutual;
import com.mutuals.streak.entity.Streak;
import com.mutuals.streak.entity.StreakEndReason;
import com.mutuals.streak.entity.StreakStatus;
import com.mutuals.streak.repository.StreakRepository;
import com.mutuals.user.entity.User;
import com.mutuals.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakMaintenanceServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 25);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    @Mock
    private StreakRepository streakRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StreakTerminationService terminationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private StreakMaintenanceService service;

    @Test
    void armedShieldCoversTheMissedDay() {
        Streak streak = streak(12, true);
        when(streakRepository.findMissed(StreakStatus.ACTIVE, YESTERDAY)).thenReturn(List.of(streak));

        StreakMaintenanceService.DayCloseResult result = service.closeDay(TODAY);

        assertThat(result.shielded()).isEqualTo(1);
        assertThat(streak.isShieldArmed()).isFalse();
        assertThat(streak.getShieldsUsed()).isEqualTo(1);
        assertThat(streak.getLastActiveDate()).isEqualTo(YESTERDAY);
        assertThat(streak.isPure()).isFalse();
        verify(eventPublisher).publishEvent(any(ShieldUsedEvent.class));
        verify(terminationService, never()).breakStreak(any(), any(), any());
    }

    @Test
    void streakWithoutShieldBreaks() {
        Streak streak = streak(12, false);
        when(streakRepository.findMissed(StreakStatus.ACTIVE, YESTERDAY)).thenReturn(List.of(streak));

        StreakMaintenanceService.DayCloseResult result = service.closeDay(TODAY);

        assertThat(result.broken()).isEqualTo(1);
        verify(terminationService).breakStreak(streak, StreakEndReason.MISSED_DAY, YESTERDAY);
        verify(userRepository).resetStalePersonalStreaks(YESTERDAY);
    }

    private Streak streak(int length, boolean shieldArmed) {
        User first = new User();
        ReflectionTestUtils.setField(first, "id", 1L);
        User second = new User();
        ReflectionTestUtils.setField(second, "id", 2L);
        Mutual mutual = new Mutual();
        mutual.setUserA(first);
        mutual.setUserB(second);
        Streak streak = new Streak();
        streak.setMutual(mutual);
        streak.setCurrentLength(length);
        streak.setStartDate(TODAY.minusDays(20));
        streak.setLastActiveDate(TODAY.minusDays(2));
        streak.setShieldArmed(shieldArmed);
        return streak;
    }
}
