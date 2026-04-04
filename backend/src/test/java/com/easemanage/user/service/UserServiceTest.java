package com.easemanage.user.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.user.dto.CreateUserRequest;
import com.easemanage.user.dto.UserResponse;
import com.easemanage.user.entity.Role;
import com.easemanage.user.entity.Status;
import com.easemanage.user.entity.User;
import com.easemanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser", "newuser@test.com", "password123",
                "John", "Doe", Role.MANAGER
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.role()).isEqualTo("MANAGER");
        verify(userRepository).save(any(User.class));
        verify(auditService).log(eq("User"), eq(1L), eq("CREATE"), isNull(), anyString());
    }

    @Test
    void createUser_duplicateUsername_throwsDuplicateResourceException() {
        CreateUserRequest request = new CreateUserRequest(
                "existing", "new@test.com", "password123",
                "Jane", "Doe", Role.VIEWER
        );

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_lastAdmin_throwsIllegalArgumentException() {
        User adminUser = buildTestUser(1L, "admin", Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete the last admin");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    private User buildTestUser(Long id, String username, Role role) {
        User user = User.builder()
                .username(username)
                .email(username + "@test.com")
                .passwordHash("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .status(Status.ACTIVE)
                .build();
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
