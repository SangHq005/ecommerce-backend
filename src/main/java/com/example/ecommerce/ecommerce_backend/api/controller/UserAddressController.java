package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.profile.AddressRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.user.AddressService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@PreAuthorize("hasRole('CLIENT')")
@Tag(name = "User Addresses", description = "User address management")
public class UserAddressController {

    private final AddressService addressService;

    public UserAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    @Operation(summary = "List addresses", description = "Get all user addresses")
    public ResponseEntity<ApiResponse<List<UserAddressEntity>>> list(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        List<UserAddressEntity> addresses = addressService.list(userId);
        return ResponseHelper.ok(addresses);
    }

    @PostMapping
    @Operation(summary = "Create address", description = "Add a new address")
    public ResponseEntity<ApiResponse<UserAddressEntity>> create(
            Authentication auth,
            @Valid @RequestBody AddressRequest req
    ) {
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
        a.setAddressType(req.addressType());
        if (Boolean.TRUE.equals(req.isDefault())) {
            a.setDefault(true);
        }
        UserAddressEntity created = addressService.create(userId, a);
        return ResponseHelper.created(created, "Address created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Update an existing address")
    public ResponseEntity<ApiResponse<UserAddressEntity>> update(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest req
    ) {
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
        patch.setAddressType(req.addressType());
        if (Boolean.TRUE.equals(req.isDefault())) {
            patch.setDefault(true);
        }
        UserAddressEntity updated = addressService.update(userId, id, patch);
        return ResponseHelper.ok(updated, "Address updated successfully");
    }

    @PostMapping("/{id}/default")
    @Operation(summary = "Set default address", description = "Set address as default")
    public ResponseEntity<ApiResponse<Void>> setDefault(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        addressService.setDefault(userId, id);
        return ResponseHelper.ok(null, "Default address updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication auth, @PathVariable Long id) {
        Long userId = Long.valueOf(auth.getName());
        addressService.delete(userId, id);
        return ResponseHelper.ok(null, "Address deleted successfully");
    }
}
