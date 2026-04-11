package me.nimnakse.water_management.auth.repository;

import java.util.Optional;
import me.nimnakse.water_management.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findByUsername(String username);

    void deleteByUsername(String username);
}
