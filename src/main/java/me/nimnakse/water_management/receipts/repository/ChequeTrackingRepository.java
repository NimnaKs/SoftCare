package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.ChequeTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChequeTrackingRepository extends JpaRepository<ChequeTracking, Long> {
    Optional<ChequeTracking> findByChequeNo(String chequeNo);
}
