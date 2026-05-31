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
    @Column(name = "ticket_no", nullable = false, length = 50)
    private String ticketNo;

    @Column(name = "org_unit_id", nullable = false)
    private Long orgUnitId;

    @Column(name = "connection_id")
    private Long connectionId;

    @Column(name = "customer_name_snapshot", length = 255)
    private String customerNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_group", nullable = false)
    private ServiceRequestGroup requestGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestCategory category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStatus status = ServiceRequestStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false)
    private ServiceRequestStageType currentStage = ServiceRequestStageType.REQUEST;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "last_paused_at")
    private Instant lastPausedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "expiry_at")
    private Instant expiryAt;

    @Column(name = "total_paused_minutes", nullable = false)
    private Long totalPausedMinutes = 0L;

    @Column(name = "connection_tariff_id")
    private Long connectionTariffId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
