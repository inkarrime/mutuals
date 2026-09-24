package com.mutuals.avatar.service;

import com.mutuals.achievement.entity.Achievement;
import com.mutuals.achievement.repository.UserAchievementRepository;
import com.mutuals.avatar.dto.AvatarItemResponse;
import com.mutuals.avatar.entity.AvatarItem;
import com.mutuals.avatar.entity.UserAvatarItem;
import com.mutuals.avatar.repository.AvatarItemRepository;
import com.mutuals.avatar.repository.UserAvatarItemRepository;
import com.mutuals.common.exception.DuplicateResourceException;
import com.mutuals.common.exception.ForbiddenOperationException;
import com.mutuals.common.exception.ResourceNotFoundException;
import com.mutuals.economy.service.WalletService;
import com.mutuals.security.CurrentUserService;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarItemRepository itemRepository;
    private final UserAvatarItemRepository userItemRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final WalletService walletService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<AvatarItemResponse> shop() {
        Map<Long, UserAvatarItem> owned = ownedByCurrentUser();
        return itemRepository.findByActiveTrueOrderByLayerAscPriceGemsAsc().stream()
                .map(item -> toResponse(item, owned.get(item.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvatarItemResponse> myItems() {
        return userItemRepository.findAllByUserWithItem(currentUserService.getCurrentUserId()).stream()
                .map(owned -> toResponse(owned.getItem(), owned))
                .toList();
    }

    @Transactional
    public AvatarItemResponse purchase(Long itemId) {
        User me = currentUserService.getCurrentUser();
        AvatarItem item = requireActiveItem(itemId);
        if (userItemRepository.existsByUserIdAndItemId(me.getId(), itemId)) {
            throw new DuplicateResourceException("You already own this item");
        }
        if (item.isPremiumOnly() && !me.hasRole(Role.PREMIUM)) {
            throw new ForbiddenOperationException("This item is exclusive to Mutuals Plus");
        }
        Achievement required = item.getUnlockAchievement();
        if (required != null && !userAchievementRepository.existsByUserIdAndAchievementId(me.getId(), required.getId())) {
            throw new ForbiddenOperationException("Unlock the achievement " + required.getName() + " first");
        }
        walletService.spendGems(me, item.getPriceGems(), "Avatar item: " + item.getName());
        UserAvatarItem owned = new UserAvatarItem();
        owned.setUser(me);
        owned.setItem(item);
        return toResponse(item, userItemRepository.save(owned));
    }

    @Transactional
    public AvatarItemResponse equip(Long itemId) {
        Long userId = currentUserService.getCurrentUserId();
        UserAvatarItem owned = userItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("You do not own avatar item " + itemId));
        userItemRepository.findByUserIdAndEquippedTrueAndItemLayer(userId, owned.getItem().getLayer())
                .forEach(current -> current.setEquipped(false));
        owned.setEquipped(true);
        return toResponse(owned.getItem(), owned);
    }

    private AvatarItem requireActiveItem(Long itemId) {
        return itemRepository.findById(itemId)
                .filter(AvatarItem::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar item", itemId));
    }

    private Map<Long, UserAvatarItem> ownedByCurrentUser() {
        return userItemRepository.findAllByUserWithItem(currentUserService.getCurrentUserId()).stream()
                .collect(Collectors.toMap(owned -> owned.getItem().getId(), Function.identity()));
    }

    private AvatarItemResponse toResponse(AvatarItem item, UserAvatarItem owned) {
        return new AvatarItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getLayer(),
                item.getPriceGems(),
                item.isPremiumOnly(),
                item.getUnlockAchievement() == null ? null : item.getUnlockAchievement().getCode(),
                item.getImageUrl(),
                owned != null,
                owned != null && owned.isEquipped());
    }
}
