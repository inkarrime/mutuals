package com.mutuals.user.controller;

import com.mutuals.common.dto.PageResponse;
import com.mutuals.social.service.FollowService;
import com.mutuals.user.dto.CustomizationResponse;
import com.mutuals.user.dto.DeviceResponse;
import com.mutuals.user.dto.MeResponse;
import com.mutuals.user.dto.PreferencesResponse;
import com.mutuals.user.dto.PublicProfileResponse;
import com.mutuals.user.dto.RegisterDeviceRequest;
import com.mutuals.user.dto.UpdateCustomizationRequest;
import com.mutuals.user.dto.UpdatePreferencesRequest;
import com.mutuals.user.dto.UpdateProfileRequest;
import com.mutuals.user.dto.UserSummaryResponse;
import com.mutuals.user.service.AccountService;
import com.mutuals.user.service.DeviceService;
import com.mutuals.user.service.UserProfileService;
import com.mutuals.user.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users", description = "Perfil propio, perfiles públicos, preferencias, personalización y dispositivos")
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService profileService;
    private final UserSettingsService settingsService;
    private final DeviceService deviceService;
    private final AccountService accountService;
    private final FollowService followService;

    @Operation(summary = "Mi perfil")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(profileService.getMe());
    }

    @Operation(summary = "Actualizar mi perfil")
    @PatchMapping("/me")
    public ResponseEntity<MeResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateMe(request));
    }

    @Operation(summary = "Eliminar mi cuenta (se anonimiza y el historial de otros la muestra como eliminada)")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        accountService.deleteMyAccount();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mis preferencias (ubicación, notificaciones, idioma)")
    @GetMapping("/me/preferences")
    public ResponseEntity<PreferencesResponse> preferences() {
        return ResponseEntity.ok(settingsService.getPreferences());
    }

    @Operation(summary = "Actualizar preferencias (usado por onboarding y settings)")
    @PatchMapping("/me/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(@Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(settingsService.updatePreferences(request));
    }

    @Operation(summary = "Personalización del perfil")
    @GetMapping("/me/customization")
    public ResponseEntity<CustomizationResponse> customization() {
        return ResponseEntity.ok(settingsService.getCustomization());
    }

    @Operation(summary = "Actualizar personalización (temas, fueguito, ícono y widgets requieren Plus)")
    @PatchMapping("/me/customization")
    public ResponseEntity<CustomizationResponse> updateCustomization(
            @Valid @RequestBody UpdateCustomizationRequest request) {
        return ResponseEntity.ok(settingsService.updateCustomization(request));
    }

    @Operation(summary = "Registrar un dispositivo para notificaciones push")
    @PostMapping("/me/devices")
    public ResponseEntity<DeviceResponse> registerDevice(@Valid @RequestBody RegisterDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.register(request));
    }

    @Operation(summary = "Mis dispositivos")
    @GetMapping("/me/devices")
    public ResponseEntity<List<DeviceResponse>> devices() {
        return ResponseEntity.ok(deviceService.listMine());
    }

    @Operation(summary = "Eliminar un dispositivo")
    @DeleteMapping("/me/devices/{deviceId}")
    public ResponseEntity<Void> unregisterDevice(@PathVariable Long deviceId) {
        deviceService.unregister(deviceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar usuarios por username")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserSummaryResponse>> search(
            @RequestParam @Size(min = 2, max = 30) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(profileService.search(q, pageable));
    }

    @Operation(summary = "Perfil público por id")
    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileResponse> profile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }

    @Operation(summary = "Perfil público por username")
    @GetMapping("/by-username/{username}")
    public ResponseEntity<PublicProfileResponse> profileByUsername(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getPublicProfileByUsername(username));
    }

    @Operation(summary = "Seguidores de un usuario")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<PageResponse<UserSummaryResponse>> followers(@PathVariable Long userId,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.followers(userId, pageable));
    }

    @Operation(summary = "Usuarios que sigue un usuario")
    @GetMapping("/{userId}/following")
    public ResponseEntity<PageResponse<UserSummaryResponse>> following(@PathVariable Long userId,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(followService.following(userId, pageable));
    }
}
