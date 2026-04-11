package me.nimnakse.water_management.auth.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import me.nimnakse.water_management.auth.dto.PasswordResetConfirmReq;
import me.nimnakse.water_management.auth.dto.PasswordResetOtpReq;
import me.nimnakse.water_management.auth.entity.PasswordResetOtp;
import me.nimnakse.water_management.auth.repository.PasswordResetOtpRepository;
import me.nimnakse.water_management.auth.service.PasswordResetService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.sms.service.SmsNotificationService;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsNotificationService smsNotificationService;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetOtpRepository passwordResetOtpRepository,
            PasswordEncoder passwordEncoder,
            SmsNotificationService smsNotificationService) {
        this.userRepository = userRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsNotificationService = smsNotificationService;
    }

    @Transactional
    @Override
    public void requestOtp(PasswordResetOtpReq request) {
        User user = findUser(request.username());
        String normalizedUsername = normalizeUsername(request.username());
        String otp = generateOtp();

        PasswordResetOtp record = passwordResetOtpRepository.findByUsername(normalizedUsername)
                .orElseGet(PasswordResetOtp::new);
        record.setUsername(normalizedUsername);
        record.setOtpCode(otp);
        record.setExpiresAt(Instant.now().plus(OTP_TTL));
        passwordResetOtpRepository.save(record);

        smsNotificationService.sendPasswordResetOtp(user.getMobileNumber(), otp);
    }

    @Transactional
    @Override
    public void confirmReset(PasswordResetConfirmReq request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match", "à¶¸à·”à¶»à¶´à¶¯ à¶¯à·™à¶š à¶±à·œà¶œà·à¶½à¶´à·š", ErrorCode.VALIDATION_ERROR);
        }

        String normalizedUsername = normalizeUsername(request.username());
        PasswordResetOtp record = passwordResetOtpRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new BadRequestException("OTP is invalid or expired", "OTP à¶šà·šà¶­à¶º à·€à¶½à¶‚à¶œà·” à¶±à·à¶­ à·„à· à¶šà¶½à·Š à¶‰à¶šà·”à¶­à·Š à·€à·“ à¶‡à¶­", ErrorCode.VALIDATION_ERROR));

        if (record.getExpiresAt().isBefore(Instant.now())) {
            passwordResetOtpRepository.delete(record);
            throw new BadRequestException("OTP is invalid or expired", "OTP à¶šà·šà¶­à¶º à·€à¶½à¶‚à¶œà·” à¶±à·à¶­ à·„à· à¶šà¶½à·Š à¶‰à¶šà·”à¶­à·Š à·€à·“ à¶‡à¶­", ErrorCode.VALIDATION_ERROR);
        }

        if (!record.getOtpCode().equals(request.otp().trim())) {
            throw new BadRequestException("OTP is invalid", "OTP à¶šà·šà¶­à¶º à·€à·à¶»à¶¯à·’à¶ºà·’", ErrorCode.VALIDATION_ERROR);
        }

        User user = findUser(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        passwordResetOtpRepository.delete(record);
    }

    private User findUser(String username) {
        String normalizedUsername = normalizeUsername(username);
        return userRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new NotFoundException("User not found", "à¶´à¶»à·’à·à·“à¶½à¶šà¶ºà· à·ƒà·œà¶ºà·à¶œà¶­ à¶±à·œà·„à·à¶š", ErrorCode.USER_NOT_FOUND));
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }
}
