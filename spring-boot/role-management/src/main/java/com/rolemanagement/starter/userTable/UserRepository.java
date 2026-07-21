package com.rolemanagement.starter.userTable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserTable, Long> {

    Optional<UserTable> findByEmail(String email);

    Optional<UserTable> findByVerificationToken(String verificationToken);

    Optional<UserTable> findByResetPasswordToken(String resetPasswordToken);
}