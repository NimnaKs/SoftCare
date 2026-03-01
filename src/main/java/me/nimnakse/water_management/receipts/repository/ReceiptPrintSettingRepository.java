package me.nimnakse.water_management.receipts.repository;

import me.nimnakse.water_management.receipts.entity.ReceiptPrintSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptPrintSettingRepository extends JpaRepository<ReceiptPrintSetting, Long> {
    Optional<ReceiptPrintSetting> findByOrgUnitId(Long orgUnitId);
}
