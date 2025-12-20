package me.nimnakse.water_management.members.dto.response;

import java.time.OffsetDateTime;
import me.nimnakse.water_management.members.entity.MemberType;

public record MemberRes(
        Long id,
        String membershipCode,
        Long orgUnitId,
        MemberType membershipType,
        String salutation,
        String fullName,
        String corporateName,
        String displayName,
        String nicOld,
        String nicNew,
        String mobileNumber,
        String dpNicFrontUrl,
        String dpNicRearUrl,
        String signatureUrl,
        String brcDocumentUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
