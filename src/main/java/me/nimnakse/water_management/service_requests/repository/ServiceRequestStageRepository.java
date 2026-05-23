package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStage;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStageType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestStageRepository extends JpaRepository<ServiceRequestStage, Long> {
    List<ServiceRequestStage> findByServiceRequestIdOrderByIdAsc(Long serviceRequestId);

    Optional<ServiceRequestStage> findByServiceRequestIdAndStageType(Long serviceRequestId, ServiceRequestStageType stageType);
}
