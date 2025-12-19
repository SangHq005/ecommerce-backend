package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity;

import com.example.ecommerce.ecommerce_backend.domain.model.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_profile")
public class UserProfileEntity {
    @Setter
    @Getter
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Setter
    @Getter
    @Column(length = 32)
    private String phone;

    @Column(name = "gender")
    Gender gender;

    @Setter
    @Getter
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Setter
    @Getter
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private long version;

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }
}
