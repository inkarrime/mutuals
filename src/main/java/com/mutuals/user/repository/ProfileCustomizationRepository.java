package com.mutuals.user.repository;

import com.mutuals.user.entity.ProfileCustomization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileCustomizationRepository extends JpaRepository<ProfileCustomization, Long> {

    Optional<ProfileCustomization> findByUserId(Long userId);
}
