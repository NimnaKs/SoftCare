package me.nimnakse.water_management.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.CreatedOnlyEntity;

@Getter
@Setter
@Entity
@Table(name = "authorized_officers")
public class AuthorizedOfficer extends CreatedOnlyEntity {
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 100)
    private String designation;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 20)
    private String nic;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "stamp_photo_url", length = 500)
    private String stampPhotoUrl;

    @Column(name = "signature_photo_url", length = 500)
    private String signaturePhotoUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
