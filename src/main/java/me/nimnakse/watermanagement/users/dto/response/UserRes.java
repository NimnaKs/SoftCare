package me.nimnakse.watermanagement.users.dto.response;

import java.util.List;
import me.nimnakse.watermanagement.roles.dto.RoleRes;
import me.nimnakse.watermanagement.users.entity.UserStatus;

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
