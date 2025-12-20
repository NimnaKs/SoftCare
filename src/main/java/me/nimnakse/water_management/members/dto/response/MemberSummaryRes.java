package me.nimnakse.water_management.members.dto.response;

import me.nimnakse.water_management.members.entity.MemberType;

public record MemberSummaryRes(
        Long id,
        String membershipCode,
        MemberType membershipType,
        String displayName,
        String nicOld,
        String nicNew,
        String mobileNumber
) {
}
