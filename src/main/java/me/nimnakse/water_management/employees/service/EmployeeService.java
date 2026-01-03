package me.nimnakse.water_management.employees.service;

import me.nimnakse.water_management.employees.dto.request.EmployeeCreateReq;
import me.nimnakse.water_management.employees.dto.request.EmployeeUpdateReq;
import me.nimnakse.water_management.employees.dto.response.EmployeeRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface EmployeeService {
    EmployeeRes create(EmployeeCreateReq request);

    EmployeeRes update(Long id, EmployeeUpdateReq request);

    EmployeeRes getById(Long id);

    PageResponse<EmployeeRes> getPage(int page, int size);

    void deactivate(Long id);
}
