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
@Table(name = "org_notification_contacts")
public class OrgNotificationContact extends CreatedOnlyEntity {
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "priority_order", nullable = false)
    private Integer priorityOrder;
}
