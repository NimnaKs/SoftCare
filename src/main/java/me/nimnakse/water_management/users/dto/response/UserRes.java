package me.nimnakse.water_management.users.dto.response;

import java.util.List;
import me.nimnakse.water_management.roles.dto.RoleRes;
import me.nimnakse.water_management.users.entity.UserStatus;

public record UserRes(
        Long id,
        String username,
        String nic,
        String name,
        String mobileNumber,
        String secondaryContactNumber,
        String address,
        String profilePhotoUrl,
        UserStatus status,
        Long orgUnitId,
        List<RoleRes> roles
) {
}
