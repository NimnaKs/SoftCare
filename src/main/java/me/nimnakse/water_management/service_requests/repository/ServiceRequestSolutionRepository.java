package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestResolutionType;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestSolution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestSolutionRepository extends JpaRepository<ServiceRequestSolution, Long> {
    List<ServiceRequestSolution> findByServiceRequestIdOrderByUpdatedAtDesc(Long serviceRequestId);

    Optional<ServiceRequestSolution> findTopByServiceRequestIdOrderByUpdatedAtDesc(Long serviceRequestId);

    Optional<ServiceRequestSolution> findTopByServiceRequestIdAndResolutionTypeOrderByUpdatedAtDesc(Long serviceRequestId, ServiceRequestResolutionType resolutionType);
}
