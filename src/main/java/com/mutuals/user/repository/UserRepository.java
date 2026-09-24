package com.mutuals.user.repository;

import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    Page<User> findByUsernameStartingWithAndStatus(String prefix, UserStatus status, Pageable pageable);

    List<User> findByStatus(UserStatus status);

    long countByStatus(UserStatus status);

    @Modifying
    @Query("""
            update User u set u.personalStreak = 0
            where u.personalStreak > 0 and u.personalStreakLastDate < :yesterday
            """)
    int resetStalePersonalStreaks(@Param("yesterday") LocalDate yesterday);
}
