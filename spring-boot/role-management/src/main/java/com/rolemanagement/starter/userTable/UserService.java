package com.rolemanagement.starter.userTable;

import com.rolemanagement.starter.common.exception.ConflictException;
import com.rolemanagement.starter.common.exception.ForbiddenException;
import com.rolemanagement.starter.common.exception.NotFoundException;
import com.rolemanagement.starter.config.EmailVerificationConfig;
import com.rolemanagement.starter.config.JwtHelper;
import com.rolemanagement.starter.email.EmailService;
import com.rolemanagement.starter.userTable.dto.LoginResponse;
import com.rolemanagement.starter.userTable.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
}