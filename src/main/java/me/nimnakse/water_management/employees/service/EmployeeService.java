package me.nimnakse.water_management.employees.service;

import me.nimnakse.water_management.employees.dto.request.EmployeeCreateReq;
import me.nimnakse.water_management.employees.dto.request.EmployeeUpdateReq;
import me.nimnakse.water_management.employees.dto.response.EmployeeRes;

public interface EmployeeService {
    EmployeeRes create(EmployeeCreateReq request);

    EmployeeRes update(Long id, EmployeeUpdateReq request);

    EmployeeRes getById(Long id);

    void deactivate(Long id);
}
