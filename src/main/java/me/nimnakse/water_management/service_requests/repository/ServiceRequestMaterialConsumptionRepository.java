package me.nimnakse.water_management.service_requests.repository;

import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestMaterialConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestMaterialConsumptionRepository extends JpaRepository<ServiceRequestMaterialConsumption, Long> {
    Optional<ServiceRequestMaterialConsumption> findByServiceRequestId(Long serviceRequestId);
}
