package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.profile.AddressRequest;
import com.example.ecommerce.ecommerce_backend.application.service.AddressService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
public class UserAddressController {

    private final AddressService addressService;

    public UserAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<UserAddressEntity> list(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return addressService.list(userId);
    }

    @PostMapping
    public UserAddressEntity create(Authentication auth, @Valid @RequestBody AddressRequest req) {
        Long userId = Long.valueOf(auth.getName());
        UserAddressEntity a = new UserAddressEntity();
        a.setReceiverName(req.receiverName());
        a.setReceiverPhone(req.receiverPhone());
        a.setLine1(req.line1());
        a.setLine2(req.line2());
        a.setWard(req.ward());
        a.setDistrict(req.district());
        a.setProvince(req.province());
        a.setPostalCode(req.postalCode());
        return addressService.create(userId, a);
    }

    @PutMapping("/{id}")
    public UserAddressEntity update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        Long userId = Long.valueOf(auth.getName());
        UserAddressEntity patch = new UserAddressEntity();
        patch.setReceiverName(req.receiverName());
        patch.setReceiverPhone(req.receiverPhone());
        patch.setLine1(req.line1());
        patch.setLine2(req.line2());
        patch.setWard(req.ward());
        patch.setDistrict(req.district());
        patch.setProvince(req.province());
        patch.setPostalCode(req.postalCode());
        return addressService.update(userId, id, patch);
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        addressService.setDefault(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        addressService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
