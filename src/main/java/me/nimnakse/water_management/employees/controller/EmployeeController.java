package me.nimnakse.water_management.employees.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.nimnakse.water_management.common.api.ApiResponse;
import me.nimnakse.water_management.common.api.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employees", description = "Employee management operations")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates a new employee record.")
    public ResponseEntity<ApiResponse<EmployeeRes>> create(@Valid @RequestBody EmployeeCreateReq request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Updates an existing employee.")
    public ResponseEntity<ApiResponse<EmployeeRes>> update(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeUpdateReq request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.update(id, request)));
    }

    @GetMapping
    @Operation(summary = "List employees", description = "Returns employees in a paginated list ordered by last update.")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeRes>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getPage(page, size)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate employee", description = "Marks an employee as deactivated.")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        employeeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee", description = "Fetches an employee by identifier.")
    public ResponseEntity<ApiResponse<EmployeeRes>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getById(id)));
    }
}
