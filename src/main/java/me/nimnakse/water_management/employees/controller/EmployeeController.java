package me.nimnakse.water_management.employees.controller;

import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.employees.dto.request.EmployeeCreateReq;
import me.nimnakse.water_management.employees.dto.request.EmployeeUpdateReq;
import me.nimnakse.water_management.employees.dto.response.EmployeeRes;
import me.nimnakse.water_management.employees.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeRes>> create(@Valid @RequestBody EmployeeCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeRes>> update(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getById(id)));
    }
}
