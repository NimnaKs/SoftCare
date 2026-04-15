package me.nimnakse.water_management.service_requests.repository;

import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestFeedbackRepository extends JpaRepository<ServiceRequestFeedback, Long> {
    Optional<ServiceRequestFeedback> findByServiceRequestId(Long serviceRequestId);
}
