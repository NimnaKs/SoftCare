package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestTimelineEventRepository extends JpaRepository<ServiceRequestTimelineEvent, Long> {
    List<ServiceRequestTimelineEvent> findByServiceRequestIdOrderByCreatedAtAsc(Long serviceRequestId);
}
