package com.mutuals.user.repository;

import com.mutuals.user.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByFcmToken(String fcmToken);

    Optional<Device> findByIdAndUserId(Long id, Long userId);

    List<Device> findByUserId(Long userId);
}
