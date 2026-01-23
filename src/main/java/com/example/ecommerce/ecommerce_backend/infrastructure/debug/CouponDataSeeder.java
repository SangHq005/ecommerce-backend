package com.example.ecommerce.ecommerce_backend.infrastructure.debug;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CouponJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class CouponDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CouponDataSeeder.class);

    private final CouponJpaRepository couponRepo;

    public CouponDataSeeder(CouponJpaRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (couponRepo.count() > 0) {
            log.info("Coupons already exist, skipping seeding.");
            return;
        }

        log.info("Seeding demo coupons...");

        // Coupon 1: WELCOME50 - Fixed 50k off
        CouponEntity c1 = new CouponEntity();
        c1.setCode("WELCOME50");
        c1.setName("Chào bạn mới");
        c1.setDescription("Giảm ngay 50.000đ cho đơn hàng đầu tiên");
        c1.setType(CouponType.FIXED_AMOUNT);
        c1.setStatus(CouponStatus.ACTIVE);
        c1.setDiscountValue(50000L);
        c1.setMinOrderAmount(100000L); // Min order 100k
        c1.setStartDate(Instant.now().minus(1, ChronoUnit.DAYS));
        c1.setEndDate(Instant.now().plus(365, ChronoUnit.DAYS));
        c1.setUsageLimit(1000);
        c1.setUsageLimitPerUser(1);
        c1.setAutoApply(false);
        couponRepo.save(c1);

        // Coupon 2: SALE10 - 10% off
        CouponEntity c2 = new CouponEntity();
        c2.setCode("SALE10");
        c2.setName("Siêu Sale 10%");
        c2.setDescription("Giảm 10% tối đa 100k cho mọi đơn hàng");
        c2.setType(CouponType.PERCENTAGE);
        c2.setStatus(CouponStatus.ACTIVE);
        c2.setDiscountValue(10L); // 10%
        c2.setMaxDiscountAmount(100000L);
        c2.setMinOrderAmount(0L);
        c2.setStartDate(Instant.now().minus(1, ChronoUnit.DAYS));
        c2.setEndDate(Instant.now().plus(30, ChronoUnit.DAYS));
        c2.setUsageLimit(500);
        c2.setUsageLimitPerUser(5);
        c2.setAutoApply(false);
        couponRepo.save(c2);

        log.info("Demo coupons seeded: WELCOME50, SALE10");
    }
}
