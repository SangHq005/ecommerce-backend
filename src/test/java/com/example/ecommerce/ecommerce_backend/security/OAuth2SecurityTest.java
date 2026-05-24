package com.example.ecommerce.ecommerce_backend.security;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.application.service.auth.AuthService;
import com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

/**
 * OAuth2 Security Edge Case Tests
 * 
 * Tests cover:
 * - New user registration via OAuth2
 * - Existing user linking Google account
 * - Disabled user attempting OAuth2 login
 * - Email collision handling
 * - Token generation after OAuth2 flow
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OAuth2 Security Edge Cases")
class OAuth2SecurityTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private RoleJpaRepository roleRepository;

    private RoleEntity clientRole;

    @BeforeEach
    void setUp() {
        // Ensure CLIENT role exists
        clientRole = roleRepository.findByCode("CLIENT")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setCode("CLIENT");
                    role.setName("Client");
                    return roleRepository.save(role);
                });
    }

    @Nested
    @DisplayName("New User OAuth2 Registration")
    class NewUserOAuth2Tests {

        @Test
        @DisplayName("Should create new user when OAuth2 login with unknown Google account")
        void oauth2Login_CreatesNewUser_WhenGoogleAccountUnknown() {
            // Arrange
            String googleSub = "google_" + UUID.randomUUID();
            String email = "oauth2-new-" + UUID.randomUUID() + "@test.com";
            String fullName = "New OAuth2 User";

            // Ensure user doesn't exist
            assertFalse(userRepository.findByEmail(email.toLowerCase()).isPresent());

            // Act
            JwtService.TokenPair tokens = authService.oauth2LoginOrLink(
                    googleSub, email, fullName, "Test-Agent", "127.0.0.1"
            );

            // Assert
            assertNotNull(tokens);
            assertNotNull(tokens.accessToken());
            assertNotNull(tokens.refreshToken());

            // Verify user was created
            Optional<UserEntity> createdUser = userRepository.findByEmail(email.toLowerCase());
            assertTrue(createdUser.isPresent());
            assertEquals(fullName, createdUser.get().getFullName());
            assertEquals(googleSub, createdUser.get().getGoogleSub());
            assertEquals("ACTIVE", createdUser.get().getStatus());
            assertTrue(createdUser.get().getRoles().stream()
                    .anyMatch(r -> "CLIENT".equals(r.getCode())));
        }

        @Test
        @DisplayName("Should use email as name when fullName is null")
        void oauth2Login_UsesEmailAsName_WhenNameIsNull() {
            // Arrange
            String googleSub = "google_" + UUID.randomUUID();
            String email = "oauth2-noname-" + UUID.randomUUID() + "@test.com";

            // Act
            authService.oauth2LoginOrLink(googleSub, email, null, "Test-Agent", "127.0.0.1");

            // Assert
            Optional<UserEntity> user = userRepository.findByEmail(email.toLowerCase());
            assertTrue(user.isPresent());
            assertEquals(email, user.get().getFullName());
        }
    }

    @Nested
    @DisplayName("Existing User OAuth2 Linking")
    class ExistingUserLinkingTests {

        @Test
        @DisplayName("Should link Google account to existing email user")
        void oauth2Login_LinksGoogleAccount_WhenEmailExists() {
            // Arrange - Create user without Google account
            UserEntity existingUser = new UserEntity();
            existingUser.setEmail("existing-" + UUID.randomUUID() + "@test.com");
            existingUser.setPasswordHash("$2a$10$hashedpassword");
            existingUser.setFullName("Existing User");
            existingUser.setStatus("ACTIVE");
            existingUser.getRoles().add(clientRole);
            existingUser = userRepository.save(existingUser);

            String googleSub = "google_link_" + UUID.randomUUID();

            assertNull(existingUser.getGoogleSub());

            // Act
            JwtService.TokenPair tokens = authService.oauth2LoginOrLink(
                    googleSub, existingUser.getEmail(), "OAuth Name", "Test-Agent", "127.0.0.1"
            );

            // Assert
            assertNotNull(tokens);

            // Verify Google account was linked
            UserEntity updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
            assertEquals(googleSub, updatedUser.getGoogleSub());
            // Original name should be preserved
            assertEquals("Existing User", updatedUser.getFullName());
        }

        @Test
        @DisplayName("Should return tokens for existing user with same Google account")
        void oauth2Login_ReturnTokens_WhenGoogleAccountAlreadyLinked() {
            // Arrange - Create user with Google account
            String googleSub = "google_existing_" + UUID.randomUUID();
            String email = "google-linked-" + UUID.randomUUID() + "@test.com";

            UserEntity existingUser = new UserEntity();
            existingUser.setEmail(email);
            existingUser.setFullName("Google User");
            existingUser.setStatus("ACTIVE");
            existingUser.setGoogleSub(googleSub);
            existingUser.getRoles().add(clientRole);
            userRepository.save(existingUser);

            // Act - Login again with same Google account
            JwtService.TokenPair tokens = authService.oauth2LoginOrLink(
                    googleSub, email, "Google User", "Test-Agent", "127.0.0.1"
            );

            // Assert
            assertNotNull(tokens);
            assertNotNull(tokens.accessToken());

            // Verify only one user exists
            assertEquals(1, userRepository.findAll().stream()
                    .filter(u -> googleSub.equals(u.getGoogleSub()))
                    .count());
        }
    }

    @Nested
    @DisplayName("OAuth2 Error Cases")
    class OAuth2ErrorTests {

        @Test
        @DisplayName("Should throw when disabled user attempts OAuth2 login")
        void oauth2Login_Throws_WhenUserDisabled() {
            // Arrange - Create disabled user
            String email = "disabled-" + UUID.randomUUID() + "@test.com";

            UserEntity disabledUser = new UserEntity();
            disabledUser.setEmail(email);
            disabledUser.setFullName("Disabled User");
            disabledUser.setStatus("DISABLED");
            disabledUser.getRoles().add(clientRole);
            userRepository.save(disabledUser);

            String googleSub = "google_disabled_" + UUID.randomUUID();

            // Act & Assert
            Exception ex = assertThrows(IllegalArgumentException.class, () ->
                    authService.oauth2LoginOrLink(googleSub, email, "Name", "Test-Agent", "127.0.0.1")
            );
            assertTrue(ex.getMessage().contains("disabled"));
        }

        @Test
        @DisplayName("Should handle email case-insensitively")
        void oauth2Login_HandlesEmailCaseInsensitively() {
            // Arrange
            String email = "CaseSensitive-" + UUID.randomUUID() + "@TEST.COM";
            String googleSub = "google_case_" + UUID.randomUUID();

            // Create user with lowercase email
            UserEntity user = new UserEntity();
            user.setEmail(email.toLowerCase());
            user.setFullName("Case Test User");
            user.setStatus("ACTIVE");
            user.getRoles().add(clientRole);
            userRepository.save(user);

            // Act - Login with uppercase email
            JwtService.TokenPair tokens = authService.oauth2LoginOrLink(
                    googleSub, email.toUpperCase(), "Name", "Test-Agent", "127.0.0.1"
            );

            // Assert
            assertNotNull(tokens);

            // Verify same user was matched
            long userCount = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .count();
            assertEquals(1, userCount);
        }
    }

    @Nested
    @DisplayName("OAuth2 Token Validation")
    class OAuth2TokenTests {

        @Test
        @DisplayName("Should generate valid tokens after OAuth2 login")
        void oauth2Login_GeneratesValidTokens() {
            // Arrange
            String googleSub = "google_token_" + UUID.randomUUID();
            String email = "token-test-" + UUID.randomUUID() + "@test.com";

            // Act
            JwtService.TokenPair tokens = authService.oauth2LoginOrLink(
                    googleSub, email, "Token Test User", "Test-Agent", "127.0.0.1"
            );

            // Assert
            assertNotNull(tokens.accessToken());
            assertNotNull(tokens.refreshToken());
            assertFalse(tokens.accessToken().isEmpty());
            assertFalse(tokens.refreshToken().isEmpty());
            // Tokens should be different
            assertNotEquals(tokens.accessToken(), tokens.refreshToken());
        }
    }
}
