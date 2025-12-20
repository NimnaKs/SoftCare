package me.nimnakse.water_management.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "water_projects")
public class WaterProject extends BaseEntity {
    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WaterProjectStatus status = WaterProjectStatus.UNREGISTERED;

    @Column(name = "department_org_name", length = 255)
    private String departmentOrgName;

    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;
}
