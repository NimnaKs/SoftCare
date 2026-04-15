package me.nimnakse.water_management.service_requests.repository;

import java.util.List;
import me.nimnakse.water_management.service_requests.entity.ServiceRequestWorkOrderEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestWorkOrderEmployeeRepository extends JpaRepository<ServiceRequestWorkOrderEmployee, Long> {
    List<ServiceRequestWorkOrderEmployee> findByWorkOrderId(Long workOrderId);

    void deleteByWorkOrderId(Long workOrderId);
}
