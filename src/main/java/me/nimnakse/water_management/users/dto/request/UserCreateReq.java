package me.nimnakse.water_management.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import me.nimnakse.water_management.common.util.ValidationPatterns;
import me.nimnakse.water_management.roles.entity.RoleAppScope;

public record UserCreateReq(
                @NotBlank String username,
                @NotBlank String nic,
                @NotBlank String name,
                @NotBlank @Pattern(regexp = ValidationPatterns.SRI_LANKA_MOBILE_REGEX, message = "Mobile number must be a 10-digit number starting with 07") String mobileNumber,
                @Pattern(regexp = ValidationPatterns.OPTIONAL_SRI_LANKA_PHONE_REGEX, message = "Secondary contact number must be a 10-digit Sri Lankan phone number") String secondaryContactNumber,
                String address,
                String profilePhotoUrl,
                String passwordHash,
                Long orgUnitId,
                Long agencyId,
                @NotNull RoleAppScope appScope,
                @NotEmpty List<Long> roleIds) {
}
