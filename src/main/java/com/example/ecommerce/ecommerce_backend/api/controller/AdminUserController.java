package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.admin.AdminUserResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.AdminUserRolesRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.admin.AdminUserStatusRequest;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.AdminUserService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin user management")
public class AdminUserController {

    private final UserJpaRepository userRepo;
    private final AdminUserService adminUserService;

    public AdminUserController(UserJpaRepository userRepo, AdminUserService adminUserService) {
        this.userRepo = userRepo;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "List users", description = "Get paginated list of all users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AdminUserResponse> users = userRepo.findAll(pageable).map(AdminUserResponse::from);
        return ResponseHelper.page(users);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users with filters")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AdminUserResponse> users = adminUserService.search(q, status, role, pageable)
                .map(AdminUserResponse::from);
        return ResponseHelper.page(users);
    }

    @PostMapping("/{id}/disable")
    @Transactional
    @Operation(summary = "Disable user", description = "Disable a user account")
    public ResponseEntity<ApiResponse<Void>> disable(@PathVariable Long id) {
        adminUserService.updateStatus(id, "DISABLED");
        return ResponseHelper.ok(null, "User disabled");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Update user account status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusRequest request
    ) {
        AdminUserResponse user = AdminUserResponse.from(adminUserService.updateStatus(id, request.status()));
        return ResponseHelper.ok(user, "User status updated");
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update roles", description = "Update user roles")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserRolesRequest request
    ) {
        AdminUserResponse user = AdminUserResponse.from(adminUserService.updateRoles(id, request.roles()));
        return ResponseHelper.ok(user, "User roles updated");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Get user details by ID")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUser(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(AdminUserResponse::from)
                .map(ResponseHelper::ok)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.USER_NOT_FOUND, "User not found"));
    }
}
