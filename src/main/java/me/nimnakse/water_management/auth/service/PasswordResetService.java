package me.nimnakse.water_management.auth.service;

import me.nimnakse.water_management.auth.dto.PasswordResetConfirmReq;
import me.nimnakse.water_management.auth.dto.PasswordResetOtpReq;

public interface PasswordResetService {
    void requestOtp(PasswordResetOtpReq request);

    void confirmReset(PasswordResetConfirmReq request);
}
