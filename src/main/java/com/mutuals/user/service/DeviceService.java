package com.mutuals.user.service;

import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.dto.DeviceResponse;
import com.mutuals.user.dto.RegisterDeviceRequest;
import com.mutuals.user.entity.Device;
import com.mutuals.user.entity.User;
import com.mutuals.user.mapper.UserMapper;
import com.mutuals.user.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final CurrentUserService currentUserService;
    private final DeviceRepository deviceRepository;
    private final UserMapper userMapper;
    private final Clock clock;

    @Transactional
    public DeviceResponse register(RegisterDeviceRequest request) {
        User user = currentUserService.getCurrentUser();
        Device device = deviceRepository.findByFcmToken(request.fcmToken()).orElseGet(Device::new);
        device.setUser(user);
        device.setFcmToken(request.fcmToken());
        device.setPlatform(request.platform());
        device.setLastSeenAt(clock.instant());
        return userMapper.toDevice(deviceRepository.save(device));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listMine() {
        return deviceRepository.findByUserId(currentUserService.getCurrentUserId()).stream()
                .map(userMapper::toDevice)
                .toList();
    }

    @Transactional
    public void unregister(Long deviceId) {
        Device device = deviceRepository.findByIdAndUserId(deviceId, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", deviceId));
        deviceRepository.delete(device);
    }
}
