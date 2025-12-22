package me.nimnakse.water_management.employees.service.impl;

import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.common.util.ValidationUtils;
import me.nimnakse.water_management.employees.dto.request.EmployeeCreateReq;
import me.nimnakse.water_management.employees.dto.request.EmployeeUpdateReq;
import me.nimnakse.water_management.employees.dto.response.EmployeeRes;
import me.nimnakse.water_management.employees.entity.Employee;
import me.nimnakse.water_management.employees.entity.EmployeeStatus;
import me.nimnakse.water_management.employees.repository.EmployeeRepository;
import me.nimnakse.water_management.employees.service.EmployeeService;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final OrgUnitRepository orgUnitRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               OrgUnitRepository orgUnitRepository) {
        this.employeeRepository = employeeRepository;
        this.orgUnitRepository = orgUnitRepository;
    }

    @Transactional
    @Override
    public EmployeeRes create(EmployeeCreateReq request) {
        validateOrgUnit(request.orgUnitId());
        if (employeeRepository.existsByNic(request.nic())) {
            throw new BadRequestException("Employee NIC already exists");
        }
        validateMobileNumbers(request.mobileNumber(), request.secondaryContactNumber());
        Employee employee = new Employee();
        apply(employee, request);
        employee.setStatus(EmployeeStatus.ACTIVE);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    @Override
    public EmployeeRes update(Long id, EmployeeUpdateReq request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found", ErrorCode.NOT_FOUND));
        validateOrgUnit(request.orgUnitId());
        if (!employee.getNic().equals(request.nic()) && employeeRepository.existsByNic(request.nic())) {
            throw new BadRequestException("Employee NIC already exists");
        }
        validateMobileNumbers(request.mobileNumber(), request.secondaryContactNumber());
        apply(employee, request);
        return toResponse(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeRes getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found", ErrorCode.NOT_FOUND));
        return toResponse(employee);
    }

    @Transactional
    @Override
    public void deactivate(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found", ErrorCode.NOT_FOUND));
        employee.setStatus(EmployeeStatus.DEACTIVATED);
    }

    private void apply(Employee employee, EmployeeCreateReq request) {
        employee.setOrgUnitId(request.orgUnitId());
        employee.setName(request.name());
        employee.setNic(request.nic());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setDateOfAppointment(request.dateOfAppointment());
        employee.setMobileNumber(request.mobileNumber());
        employee.setSecondaryContactNumber(request.secondaryContactNumber());
        employee.setAddress(request.address());
        employee.setDesignation(request.designation());
        employee.setProfilePhotoUrl(request.profilePhotoUrl());
    }

    private void apply(Employee employee, EmployeeUpdateReq request) {
        employee.setOrgUnitId(request.orgUnitId());
        employee.setName(request.name());
        employee.setNic(request.nic());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setDateOfAppointment(request.dateOfAppointment());
        employee.setMobileNumber(request.mobileNumber());
        employee.setSecondaryContactNumber(request.secondaryContactNumber());
        employee.setAddress(request.address());
        employee.setDesignation(request.designation());
        employee.setProfilePhotoUrl(request.profilePhotoUrl());
    }

    private void validateOrgUnit(Long orgUnitId) {
        if (orgUnitId == null || !orgUnitRepository.existsById(orgUnitId)) {
            throw new NotFoundException("Org unit not found", ErrorCode.NOT_FOUND);
        }
    }

    private void validateMobileNumbers(String mobileNumber, String secondary) {
        if (!ValidationUtils.isValidSriLankaMobile(mobileNumber)) {
            throw new BadRequestException("Mobile number must be a 10-digit number starting with 07");
        }
        if (secondary != null && !secondary.isBlank() && !ValidationUtils.isValidSriLankaPhone(secondary)) {
            throw new BadRequestException("Secondary contact number must be a 10-digit Sri Lankan phone number");
        }
    }

    private EmployeeRes toResponse(Employee employee) {
        return new EmployeeRes(
                employee.getId(),
                employee.getOrgUnitId(),
                employee.getName(),
                employee.getNic(),
                employee.getDateOfBirth(),
                employee.getDateOfAppointment(),
                employee.getMobileNumber(),
                employee.getSecondaryContactNumber(),
                employee.getAddress(),
                employee.getDesignation(),
                employee.getProfilePhotoUrl(),
                employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
