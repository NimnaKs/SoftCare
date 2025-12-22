package me.nimnakse.water_management.organization.service.impl;

import java.time.Instant;
import java.util.List;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.organization.dto.request.WaterProjectCreateReq;
import me.nimnakse.water_management.organization.dto.request.WaterProjectUpdateReq;
import me.nimnakse.water_management.organization.dto.response.WaterProjectRes;
import me.nimnakse.water_management.organization.entity.WaterProject;
import me.nimnakse.water_management.organization.entity.WaterProjectStatus;
import me.nimnakse.water_management.organization.repository.WaterProjectRepository;
import me.nimnakse.water_management.organization.service.WaterProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaterProjectServiceImpl implements WaterProjectService {
    private final WaterProjectRepository waterProjectRepository;

    public WaterProjectServiceImpl(WaterProjectRepository waterProjectRepository) {
        this.waterProjectRepository = waterProjectRepository;
    }

    @Transactional
    @Override
    public WaterProjectRes create(WaterProjectCreateReq request) {
        WaterProject project = new WaterProject();
        project.setName(request.name());
        project.setStatus(request.status() == null ? WaterProjectStatus.UNREGISTERED : request.status());
        project.setDepartmentOrgName(request.departmentOrgName());
        project.setRegisteredAt(request.registeredAt());
        WaterProject saved = waterProjectRepository.save(project);
        return toResponse(saved);
    }

    @Transactional
    @Override
    public WaterProjectRes update(Long id, WaterProjectUpdateReq request) {
        WaterProject project = waterProjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Water project not found", ErrorCode.NOT_FOUND));
        project.setName(request.name());
        project.setStatus(request.status());
        project.setDepartmentOrgName(request.departmentOrgName());
        project.setRegisteredAt(request.registeredAt());
        project = waterProjectRepository.save(project);
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    @Override
    public WaterProjectRes getById(Long id) {
        WaterProject project = waterProjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Water project not found", ErrorCode.NOT_FOUND));
        return toResponse(project);
    }

    @Transactional(readOnly = true)
    @Override
    public List<WaterProjectRes> getAll() {
        return waterProjectRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        WaterProject project = waterProjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Water project not found", ErrorCode.NOT_FOUND));
        project.setDeletedAt(Instant.now());
    }

    private WaterProjectRes toResponse(WaterProject project) {
        return new WaterProjectRes(project.getId(), project.getName(), project.getStatus(),
                project.getDepartmentOrgName(), project.getRegisteredAt());
    }
}
