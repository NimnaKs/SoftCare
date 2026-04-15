package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import me.nimnakse.water_management.common.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "service_requests")
public class ServiceRequest extends BaseEntity {
    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "connection_id")
    private Long connectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_group", nullable = false)
    private ServiceRequestGroup requestGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestCategory category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "expiry_days", nullable = false)
    private Integer expiryDays = 7;

    @Column(name = "contact_mobile_number")
    private String contactMobileNumber;

    @Column(name = "connection_tariff_id")
    private Long connectionTariffId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
