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
@Table(name = "service_request_stages")
public class ServiceRequestStage extends BaseEntity {
    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false)
    private ServiceRequestStageType stageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequestStageStatus status = ServiceRequestStageStatus.NOT_STARTED;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;
}
