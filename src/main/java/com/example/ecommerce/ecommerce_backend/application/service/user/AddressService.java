package com.example.ecommerce.ecommerce_backend.application.service.user;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AddressService {

    private final UserAddressJpaRepository addressRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public AddressService(UserAddressJpaRepository addressRepo, StringRedisTemplate redis, EventLogMongoRepository eventRepo) {
        this.addressRepo = addressRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    public List<UserAddressEntity> list(Long userId) {
        return addressRepo.findByUserIdOrderByIsDefaultDescIdDesc(userId);
    }

    @Transactional
    public UserAddressEntity create(Long userId, UserAddressEntity a) {
        a.setUserId(userId);

        if (Boolean.TRUE.equals(a.isDefault())) {
            List<UserAddressEntity> defaults = addressRepo.findByUserIdAndIsDefaultTrue(userId);
            for (UserAddressEntity d : defaults) {
                d.setDefault(false);
                addressRepo.save(d);
            }
        } else {
            // If this is the first address, force it to be default
            if (addressRepo.countByUserId(userId) == 0) {
                a.setDefault(true);
            }
        }

        UserAddressEntity saved = addressRepo.save(a);

        invalidate(userId);
        eventRepo.save(new EventLogDocument("ADDRESS_CHANGED", "user_" + userId, Instant.now(), null,
                Map.of("userId", userId, "action", "CREATED", "addressId", saved.getId())));
        return saved;
    }

    @Transactional
    public UserAddressEntity update(Long userId, Long id, UserAddressEntity patch) {
        UserAddressEntity a = addressRepo.findByIdAndUserId(id, userId).orElseThrow();
        a.setReceiverName(patch.getReceiverName());
        a.setReceiverPhone(patch.getReceiverPhone());
        a.setLine1(patch.getLine1());
        a.setLine2(patch.getLine2());
        a.setWard(patch.getWard());
        a.setDistrict(patch.getDistrict());
        a.setProvince(patch.getProvince());
        a.setPostalCode(patch.getPostalCode());
        a.setAddressType(patch.getAddressType());
        
        if (patch.isDefault() && !a.isDefault()) {
             // If setting to true, handle unsetting others
             List<UserAddressEntity> defaults = addressRepo.findByUserIdAndIsDefaultTrue(userId);
             for (UserAddressEntity d : defaults) {
                 d.setDefault(false);
                 addressRepo.save(d);
             }
             a.setDefault(true);
        }
        
        UserAddressEntity saved = addressRepo.save(a);

        invalidate(userId);
        eventRepo.save(new EventLogDocument("ADDRESS_CHANGED", "user_" + userId, Instant.now(), null,
                Map.of("userId", userId, "action", "UPDATED", "addressId", id)));
        return saved;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        UserAddressEntity a = addressRepo.findByIdAndUserId(id, userId).orElseThrow();
        boolean wasDefault = a.isDefault();
        addressRepo.delete(a);

        if (wasDefault) {
            List<UserAddressEntity> rest = addressRepo.findByUserIdOrderByIsDefaultDescIdDesc(userId);
            if (!rest.isEmpty()) setDefault(userId, rest.get(0).getId());
        }

        invalidate(userId);
        eventRepo.save(new EventLogDocument("ADDRESS_CHANGED", "user_" + userId, Instant.now(), null,
                Map.of("userId", userId, "action", "DELETED", "addressId", id)));
    }

    @Transactional
    public void setDefault(Long userId, Long id) {
        List<UserAddressEntity> defaults = addressRepo.findByUserIdAndIsDefaultTrue(userId);
        for (UserAddressEntity d : defaults) {
            if (!d.getId().equals(id)) {
                d.setDefault(false);
                addressRepo.save(d);
            }
        }
        UserAddressEntity target = addressRepo.findByIdAndUserId(id, userId).orElseThrow();
        target.setDefault(true);
        addressRepo.save(target);

        invalidate(userId);
        eventRepo.save(new EventLogDocument("ADDRESS_CHANGED", "user_" + userId, Instant.now(), null,
                Map.of("userId", userId, "action", "SET_DEFAULT", "addressId", id)));
    }

    private void invalidate(Long userId) {
        redis.delete("cache:user_addresses:" + userId);
    }
}
