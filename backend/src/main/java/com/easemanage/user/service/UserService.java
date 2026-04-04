package com.easemanage.user.service;

import com.easemanage.audit.service.AuditService;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.common.exception.DuplicateResourceException;
import com.easemanage.common.exception.ResourceNotFoundException;
import com.easemanage.user.dto.*;
import com.easemanage.user.entity.Role;
import com.easemanage.user.entity.Status;
import com.easemanage.user.entity.User;
import com.easemanage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsers(int page, int size, Role role, Status status, String search) {
        Page<User> users = userRepository.searchUsers(role, status, search,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return new PagedResponse<>(
            users.getContent().stream().map(this::toResponse).toList(),
            users.getNumber(),
            users.getSize(),
            users.getTotalElements(),
            users.getTotalPages(),
            users.isLast()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(request.role())
            .status(Status.ACTIVE)
            .build();

        User saved = userRepository.save(user);
        auditService.log("User", saved.getId(), "CREATE", null, "name=" + saved.getUsername());
        return toResponse(saved);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findById(id);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(request.email());
        }
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.role() != null) user.setRole(request.role());
        if (request.status() != null) user.setStatus(request.status());

        User updated = userRepository.save(user);
        auditService.log("User", id, "UPDATE", null, "name=" + updated.getUsername());
        return toResponse(updated);
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Cannot delete the last admin user");
        }
        auditService.log("User", id, "DELETE", "name=" + user.getUsername(), null);
        userRepository.delete(user);
    }

    public UserResponse updateProfile(Long userId, String firstName, String lastName, String email) {
        User user = findById(userId);
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(email);
        }
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        return toResponse(userRepository.save(user));
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(),
            user.getFirstName(), user.getLastName(),
            user.getRole().name(), user.getStatus().name(),
            user.getAvatarUrl(), user.getCreatedAt()
        );
    }
}
