package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestWorkOrderRepository extends JpaRepository<ServiceRequestWorkOrder, Long> {
    List<ServiceRequestWorkOrder> findByServiceRequestIdOrderByUpdatedAtDesc(Long serviceRequestId);

    Optional<ServiceRequestWorkOrder> findTopByServiceRequestIdOrderByUpdatedAtDesc(Long serviceRequestId);
}
