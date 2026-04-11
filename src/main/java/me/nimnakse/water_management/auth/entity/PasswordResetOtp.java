package me.nimnakse.water_management.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "password_reset_otps", indexes = {
        @Index(name = "idx_password_reset_otps_username", columnList = "username", unique = true)
})
public class PasswordResetOtp extends BaseEntity {
    @Column(nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
