package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserJpaRepository userRepo;

    public AdminUserController(UserJpaRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<UserEntity> list() {
        return userRepo.findAll();
    }

    @PostMapping("/{id}/disable")
    @Transactional
    public void disable(@PathVariable Long id) {
        UserEntity u = userRepo.findById(id).orElseThrow();
        u.setStatus("DISABLED");
        userRepo.save(u);
    }
}
