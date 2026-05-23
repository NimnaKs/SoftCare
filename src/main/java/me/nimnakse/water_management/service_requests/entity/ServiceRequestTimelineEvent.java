package me.nimnakse.water_management.service_requests.entity;

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
@Table(name = "service_request_timeline_events")
public class ServiceRequestTimelineEvent extends BaseEntity {
    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type")
    private ServiceRequestStageType stageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ServiceRequestEventType eventType;

    @Column(length = 1000)
    private String notes;

    @Column(name = "payload_json", length = 4000)
    private String payloadJson;

    @Column(name = "created_by")
    private Long createdBy;
}
