package com.rolemanagement.starter.userTable;

import com.rolemanagement.starter.userTable.dto.AccountDto;
import com.rolemanagement.starter.userTable.dto.ChangePasswordRequest;
import com.rolemanagement.starter.userTable.dto.ForgotPasswordRequest;
import com.rolemanagement.starter.userTable.dto.ForgotPasswordResponse;
import com.rolemanagement.starter.userTable.dto.LoginRequest;
import com.rolemanagement.starter.userTable.dto.LoginResponse;
import com.rolemanagement.starter.userTable.dto.ResetPasswordRequest;
import com.rolemanagement.starter.userTable.dto.UpdateProfileRequest;
import com.rolemanagement.starter.userTable.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserDto userDto) {
        userService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return "Email verified successfully.";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse resp = userService.login(loginRequest.email(), loginRequest.password());
        ResponseCookie cookie = ResponseCookie.from("jwt", resp.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful.");
    }

    @PatchMapping("/me")
    public AccountDto updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return userService.updateProfile(userDetails.getUsername(), request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = "If that email exists, password reset instructions have been sent.";
        return userService.forgotPassword(request.email())
                .map(token -> new ForgotPasswordResponse("Email sending is disabled; use this token to reset your password.", token))
                .orElse(new ForgotPasswordResponse(message, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
