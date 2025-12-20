package me.nimnakse.water_management.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "organization_code", nullable = false, length = 50)
    private String organizationCode;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(name = "name_si", length = 255)
    private String nameSi;

    @Column(name = "name_ta", length = 255)
    private String nameTa;

    @Column(name = "address_en", length = 500)
    private String addressEn;

    @Column(name = "address_si", length = 500)
    private String addressSi;

    @Column(name = "address_ta", length = 500)
    private String addressTa;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(length = 255)
    private String email;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "telephone_number", length = 20)
    private String telephoneNumber;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;
}
