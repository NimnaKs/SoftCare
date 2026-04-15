package me.nimnakse.water_management.service_requests.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "service_request_feedback")
public class ServiceRequestFeedback {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false, unique = true)
    private Long serviceRequestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_response", nullable = false)
    private ServiceRequestFinalResponse finalResponse;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
