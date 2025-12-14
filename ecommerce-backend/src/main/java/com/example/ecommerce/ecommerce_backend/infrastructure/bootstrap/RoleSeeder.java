package com.example.ecommerce.ecommerce_backend.infrastructure.bootstrap;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleSeeder implements ApplicationRunner {

    private final RoleJpaRepository roleRepo;

    public RoleSeeder(RoleJpaRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("ADMIN");
        seed("SELLER");
        seed("CLIENT");
    }

    private void seed(String code) {
        if (!roleRepo.existsByCode(code)) {
            RoleEntity role = new RoleEntity();
            role.setCode(code);
            role.setName(code);
            roleRepo.save(role);
        }
    }
}
