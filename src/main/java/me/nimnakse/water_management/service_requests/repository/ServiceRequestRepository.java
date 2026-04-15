package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequest;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    Page<ServiceRequest> findByOrgUnitIdOrderBySavedAtDesc(Long orgUnitId, Pageable pageable);

    Page<ServiceRequest> findByOrgUnitIdAndStatusOrderBySavedAtDesc(Long orgUnitId, ServiceRequestStatus status, Pageable pageable);

    List<ServiceRequest> findByOrgUnitIdAndStatusNotOrderBySavedAtDesc(Long orgUnitId, ServiceRequestStatus status);

    List<ServiceRequest> findByOrgUnitIdAndConnectionIdOrderBySavedAtDesc(Long orgUnitId, Long connectionId);

    Optional<ServiceRequest> findByIdAndOrgUnitId(Long id, Long orgUnitId);
}
