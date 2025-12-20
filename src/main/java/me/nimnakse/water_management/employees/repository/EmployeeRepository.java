package me.nimnakse.water_management.employees.repository;

import java.util.Optional;
import me.nimnakse.water_management.employees.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNic(String nic);

    boolean existsByNic(String nic);
}
