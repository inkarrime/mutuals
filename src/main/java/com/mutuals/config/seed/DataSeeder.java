package com.mutuals.config.seed;

import com.mutuals.achievement.entity.Achievement;
import com.mutuals.achievement.entity.AchievementType;
import com.mutuals.achievement.repository.AchievementRepository;
import com.mutuals.avatar.entity.AvatarItem;
import com.mutuals.avatar.entity.AvatarLayer;
import com.mutuals.avatar.repository.AvatarItemRepository;
import com.mutuals.challenge.entity.ChallengeTemplate;
import com.mutuals.challenge.entity.ChallengeType;
import com.mutuals.challenge.repository.ChallengeTemplateRepository;
import com.mutuals.config.AppProperties;
import com.mutuals.user.entity.ProfileCustomization;
import com.mutuals.user.entity.Role;
import com.mutuals.user.entity.User;
import com.mutuals.user.entity.UserPreferences;
import com.mutuals.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ChallengeTemplateRepository templateRepository;
    private final AchievementRepository achievementRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedChallenges();
        seedAchievements();
        seedAvatarItems();
        seedAdmin();
    }

    private void seedChallenges() {
        if (templateRepository.count() > 0) {
            return;
        }
        template("Pregunta del día", "¿Cuál fue el mejor momento de tu semana? Respondan los dos y se revela al final.", ChallengeType.REMOTE, 10, false, false);
        template("Canción para ti", "Comparte una canción que te recuerde a tu amigo y cuéntale por qué.", ChallengeType.REMOTE, 10, false, false);
        template("Llamada de 10 minutos", "Llámense al menos 10 minutos, sin chat. Cuenten de qué hablaron.", ChallengeType.REMOTE, 15, false, false);
        template("Foto juntos", "Tómense una foto juntos hoy. Solo cuenta si hicieron check-in por QR o proximidad.", ChallengeType.IN_PERSON, 25, true, true);
        template("Lugar nuevo", "Vayan juntos a un lugar donde nunca hayan estado y suban una foto.", ChallengeType.IN_PERSON, 30, true, true);
        template("Tres emojis", "Describe a tu amigo con 3 emojis. Luego descubre cómo te describió.", ChallengeType.CREATIVE, 10, false, false);
        template("Recuerdo favorito", "Escribe tu recuerdo favorito con tu amigo en 3 líneas.", ChallengeType.CREATIVE, 15, false, false);
        template("Dibujo a ciegas", "Dibujen lo mismo sin verse: una casa en la playa. Suban su dibujo.", ChallengeType.CREATIVE, 15, false, true);
        template("Plan pendiente", "Propongan un plan que llevan tiempo postergando y pónganle fecha.", ChallengeType.REMOTE, 10, false, false);
        template("Almuerzo juntos", "Compartan un almuerzo o café esta semana.", ChallengeType.IN_PERSON, 20, true, false);
        log.info("Seeded challenge templates");
    }

    private void seedAchievements() {
        if (achievementRepository.count() > 0) {
            return;
        }
        achievement("FIRST_STREAK_DAY", "Primer fueguito", "Confirma tu primer día de racha", AchievementType.STREAK, 1, 5);
        achievement("STREAK_7", "Una semana juntos", "Mantén una racha de 7 días", AchievementType.STREAK, 7, 15);
        achievement("STREAK_30", "Un mes imparable", "Mantén una racha de 30 días", AchievementType.STREAK, 30, 50);
        achievement("STREAK_100", "Centenario", "Mantén una racha de 100 días", AchievementType.STREAK, 100, 150);
        achievement("STREAK_365", "Un año de conexión", "Mantén una racha de 365 días", AchievementType.STREAK, 365, 500);
        achievement("PERSONAL_7", "Siempre presente", "7 días seguidos conectando con alguien", AchievementType.PERSONAL_STREAK, 7, 10);
        achievement("PERSONAL_30", "Hábito de amistad", "30 días seguidos conectando con alguien", AchievementType.PERSONAL_STREAK, 30, 40);
        achievement("CHALLENGE_1", "Primer desafío", "Completa tu primer desafío", AchievementType.CHALLENGE, 1, 5);
        achievement("CHALLENGE_10", "Retador", "Completa 10 desafíos", AchievementType.CHALLENGE, 10, 30);
        achievement("MUTUALS_5", "Círculo cercano", "Ten 5 mutuals", AchievementType.SOCIAL, 5, 10);
        log.info("Seeded achievements");
    }

    private void seedAvatarItems() {
        if (avatarItemRepository.count() > 0) {
            return;
        }
        avatar("BASE_CLASSIC", "Base clásica", AvatarLayer.BASE, 0, false);
        avatar("HAIR_CURLY", "Cabello rizado", AvatarLayer.HAIR, 20, false);
        avatar("HAIR_BUZZ", "Corte corto", AvatarLayer.HAIR, 20, false);
        avatar("OUTFIT_HOODIE", "Polera", AvatarLayer.OUTFIT, 30, false);
        avatar("OUTFIT_UTEC", "Polo UTEC", AvatarLayer.OUTFIT, 40, false);
        avatar("ACCESSORY_CAP", "Gorra", AvatarLayer.ACCESSORY, 25, false);
        avatar("ACCESSORY_CROWN", "Corona de fuego", AvatarLayer.ACCESSORY, 80, true);
        log.info("Seeded avatar items");
    }

    private void seedAdmin() {
        String email = properties.seed().adminEmail();
        String password = properties.seed().adminPassword();
        if (email == null || email.isBlank() || password == null || password.isBlank()
                || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User admin = new User();
        admin.setEmail(email.toLowerCase(Locale.ROOT));
        admin.setUsername("admin");
        admin.setDisplayName("Mutuals Admin");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRoles(EnumSet.of(Role.USER, Role.ADMIN));
        admin.attachPreferences(new UserPreferences());
        admin.attachCustomization(new ProfileCustomization());
        userRepository.save(admin);
        log.info("Seeded admin user {}", email);
    }

    private void template(String title, String prompt, ChallengeType type, int gems, boolean presence, boolean photo) {
        ChallengeTemplate template = new ChallengeTemplate();
        template.setTitle(title);
        template.setPrompt(prompt);
        template.setType(type);
        template.setRewardGems(gems);
        template.setRequiresPresence(presence);
        template.setRequiresPhoto(photo);
        templateRepository.save(template);
    }

    private void achievement(String code, String name, String description, AchievementType type, int threshold, int gems) {
        Achievement achievement = new Achievement();
        achievement.setCode(code);
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setType(type);
        achievement.setThreshold(threshold);
        achievement.setRewardGems(gems);
        achievementRepository.save(achievement);
    }

    private void avatar(String code, String name, AvatarLayer layer, int price, boolean premiumOnly) {
        AvatarItem item = new AvatarItem();
        item.setCode(code);
        item.setName(name);
        item.setLayer(layer);
        item.setPriceGems(price);
        item.setPremiumOnly(premiumOnly);
        avatarItemRepository.save(item);
    }
}
