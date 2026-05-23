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
@Table(name = "service_request_feedback")
public class ServiceRequestFeedback extends BaseEntity {
    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_response", nullable = false)
    private ServiceRequestFinalResponse finalResponse;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "updated_by")
    private Long updatedBy;
}
