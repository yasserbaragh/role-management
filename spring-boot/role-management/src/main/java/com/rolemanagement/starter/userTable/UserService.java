package com.rolemanagement.starter.userTable;

import com.rolemanagement.starter.common.exception.ConflictException;
import com.rolemanagement.starter.common.exception.ForbiddenException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.config.EmailVerificationConfig;
import com.rolemanagement.starter.config.JwtHelper;
import com.rolemanagement.starter.email.EmailService;
import com.rolemanagement.starter.organisationMemberhsip.OrganisationMembershipRepository;
import com.rolemanagement.starter.userTable.dto.AccountDto;
import com.rolemanagement.starter.userTable.dto.ChangePasswordRequest;
import com.rolemanagement.starter.userTable.dto.LoginResponse;
import com.rolemanagement.starter.userTable.dto.ResetPasswordRequest;
import com.rolemanagement.starter.userTable.dto.UpdateProfileRequest;
import com.rolemanagement.starter.userTable.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtHelper jwtHelper;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationConfig emailVerificationConfig;
    private final EmailService emailService;
    private final OrganisationMembershipRepository organisationMembershipRepository;

    public UserTable getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public UserTable getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    public void register(UserDto user) {
        UserTable newUser = new UserTable();
        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new ConflictException("User with email " + user.email() + " already exists.");
        }
        newUser.setEmail(user.email());
        newUser.setFullName(user.fullName());

        String hashedPass = passwordEncoder.encode(user.password());
        newUser.setPassword(hashedPass);

        if (emailVerificationConfig.isEnabled()) {
            String token = UUID.randomUUID().toString();
            newUser.setEmailVerified(false);
            newUser.setVerificationToken(token);
            newUser.setVerificationTokenExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
            userRepository.save(newUser);
            emailService.sendVerificationEmail(newUser.getEmail(), token);
        } else {
            newUser.setEmailVerified(true);
            userRepository.save(newUser);
        }
    }

    public void verifyEmail(String token) {
        UserTable user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid verification token"));
        if (user.getVerificationTokenExpiresAt() == null || user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
            throw new ConflictException("Verification token expired");
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
    }

    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserTable user = getByEmail(authentication.getName());
        if (emailVerificationConfig.isEnabled() && !user.isEmailVerified()) {
            throw new ForbiddenException("Email not verified. Please check your inbox.");
        }
        String token = jwtHelper.generateToken(authentication);

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFullName()
        );
    }

    public AccountDto updateProfile(String email, UpdateProfileRequest request) {
        UserTable user = getByEmail(email);
        user.setFullName(request.fullName());
        return AccountDto.from(userRepository.save(user));
    }

    public void deleteAccount(String email) {
        UserTable user = getByEmail(email);
        if (organisationMembershipRepository.existsByUserId(user.getId())) {
            throw new ConflictException("Leave or transfer ownership of all organisations before deleting your account");
        }
        userRepository.delete(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        UserTable user = getByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ForbiddenException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public Optional<String> forgotPassword(String email) {
        Optional<UserTable> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return Optional.empty();
        }
        UserTable user = maybeUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        if (emailVerificationConfig.isEnabled()) {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return Optional.empty();
        }
        return Optional.of(token);
    }

    public void resetPassword(ResetPasswordRequest request) {
        UserTable user = userRepository.findByResetPasswordToken(request.token())
                .orElseThrow(() -> new NotFoundException("Invalid reset token"));
        if (user.getResetPasswordTokenExpiresAt() == null || user.getResetPasswordTokenExpiresAt().isBefore(Instant.now())) {
            throw new ConflictException("Reset token expired");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);
        userRepository.save(user);
    }
}