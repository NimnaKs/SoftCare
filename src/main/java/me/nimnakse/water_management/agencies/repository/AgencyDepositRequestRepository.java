package me.nimnakse.water_management.agencies.repository;

import me.nimnakse.water_management.agencies.entity.AgencyDepositRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgencyDepositRequestRepository extends JpaRepository<AgencyDepositRequest, Long> {
    List<AgencyDepositRequest> findByAgencyIdOrderByCreatedAtDesc(Long agencyId);
    List<AgencyDepositRequest> findAllByOrderByCreatedAtDesc();
}
