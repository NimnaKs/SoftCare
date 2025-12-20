package me.nimnakse.water_management.connections.repository;

import java.util.List;
import java.util.Optional;
import me.nimnakse.water_management.connections.entity.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByAccountNumber(String accountNumber);

    List<Connection> findByMemberId(Long memberId);

    List<Connection> findByMobileNumber(String mobileNumber);
}
