package me.nimnakse.water_management.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "org_units")
public class OrgUnit extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrgUnitLevel level;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "water_project_id")
    private Long waterProjectId;

    @Column(name = "organization_code", length = 50, unique = true)
    private String organizationCode;
}
