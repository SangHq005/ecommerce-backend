package com.example.ecommerce.ecommerce_backend.application.service.user;

import com.example.ecommerce.ecommerce_backend.domain.model.Gender;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserProfileEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserProfileJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    private final UserProfileJpaRepository profileRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public ProfileService(UserProfileJpaRepository profileRepo, StringRedisTemplate redis, EventLogMongoRepository eventRepo) {
        this.profileRepo = profileRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    public Optional<UserProfileEntity> getProfile(Long userId) {
        // (optional) cache: omitted here for simplicity; correctness > cache
        return profileRepo.findById(userId);
    }

    @Transactional
    public UserProfileEntity upsertProfile(
            Long userId,
            String phone,
            Gender gender,
            LocalDate dob
    ) {
        UserProfileEntity p = profileRepo.findById(userId).orElseGet(() -> {
            UserProfileEntity np = new UserProfileEntity();
            np.setUserId(userId); // BẮT BUỘC
            return np;
        });

        p.setPhone(phone);
        p.setGender(gender);
        p.setDateOfBirth(dob);

        UserProfileEntity saved = profileRepo.save(p);

        redis.delete("cache:user_profile:" + userId);

        try {
            eventRepo.save(new EventLogDocument(
                    "PROFILE_UPDATED",
                    "user_" + userId,
                    Instant.now(),
                    null,
                    Map.of(
                            "userId", userId,
                            "phone", phone,
                            "gender", gender != null ? gender.name() : null,
                            "dateOfBirth", String.valueOf(dob)
                    )
            ));
        } catch (Exception e) {
            // swallow/log warning
        }

        return saved;
    }


    @Transactional
    public UserProfileEntity updateAvatar(Long userId, String avatarUrl) {
        UserProfileEntity p = profileRepo.findById(userId).orElseGet(() -> {
            UserProfileEntity np = new UserProfileEntity();
            np.setUserId(userId);
            return np;
        });
        p.setAvatarUrl(avatarUrl);
        UserProfileEntity saved = profileRepo.save(p);
        redis.delete("cache:user_profile:" + userId);

        try{
            eventRepo.save(new EventLogDocument(
                    "PROFILE_UPDATED",
                    "user_" + userId, Instant.now(),
                    null,
                    Map.of(
                            "userId", userId,
                            "avatarUrl", avatarUrl)));
        } catch (Exception e){

        }

        return saved;
    }
}
