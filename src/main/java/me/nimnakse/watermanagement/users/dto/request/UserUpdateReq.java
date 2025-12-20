package me.nimnakse.watermanagement.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.nimnakse.watermanagement.roles.entity.RoleAppScope;

public record UserUpdateReq(
        @NotBlank String nic,
        @NotBlank String name,
        @NotBlank String mobileNumber,
        String secondaryContactNumber,
        String address,
        String profilePhotoUrl,
        Long orgUnitId,
        @NotNull RoleAppScope appScope,
        @NotEmpty List<Long> roleIds
) {
}
