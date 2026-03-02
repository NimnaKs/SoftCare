package me.nimnakse.water_management.agencies.repository;

import me.nimnakse.water_management.agencies.entity.AgencyDepositRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyDepositRequestRepository extends JpaRepository<AgencyDepositRequest, Long> {
}

