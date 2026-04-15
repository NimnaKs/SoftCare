package me.nimnakse.water_management.meter_reader_assignments.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.nimnakse.water_management.billing_zones.entity.BillingZone;
import me.nimnakse.water_management.billing_zones.repository.BillingZoneRepository;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.meter_reader_assignments.dto.request.MeterReaderAssignmentCreateReq;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentReaderRes;
import me.nimnakse.water_management.meter_reader_assignments.dto.response.MeterReaderAssignmentRes;
import me.nimnakse.water_management.meter_reader_assignments.entity.MeterReaderZoneAssignment;
import me.nimnakse.water_management.meter_reader_assignments.repository.MeterReaderZoneAssignmentRepository;
import me.nimnakse.water_management.meter_reader_assignments.service.MeterReaderAssignmentService;
import me.nimnakse.water_management.organization.entity.OrgUnit;
import me.nimnakse.water_management.organization.repository.OrgUnitRepository;
import me.nimnakse.water_management.roles.entity.RoleAppScope;
import me.nimnakse.water_management.security.OrganizationAccessService;
import me.nimnakse.water_management.users.entity.User;
import me.nimnakse.water_management.users.entity.UserRole;
import me.nimnakse.water_management.users.entity.UserStatus;
import me.nimnakse.water_management.users.repository.UserRepository;
import me.nimnakse.water_management.users.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeterReaderAssignmentServiceImpl implements MeterReaderAssignmentService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BillingZoneRepository billingZoneRepository;
    private final MeterReaderZoneAssignmentRepository assignmentRepository;
    private final OrgUnitRepository orgUnitRepository;
    private final OrganizationAccessService accessService;

    public MeterReaderAssignmentServiceImpl(UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            BillingZoneRepository billingZoneRepository,
            MeterReaderZoneAssignmentRepository assignmentRepository,
            OrgUnitRepository orgUnitRepository,
            OrganizationAccessService accessService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.billingZoneRepository = billingZoneRepository;
        this.assignmentRepository = assignmentRepository;
        this.orgUnitRepository = orgUnitRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<MeterReaderAssignmentReaderRes> listReaders(Long orgUnitId) {
        Long resolved = resolveOrgUnitId(orgUnitId);
        return userRepository.findAllByOrgUnit_IdAndStatus(resolved, UserStatus.ACTIVE).stream()
                .filter(this::isMeterAppReader)
                .map(user -> new MeterReaderAssignmentReaderRes(user.getId(), user.getName(), user.getUsername(), resolved))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MeterReaderAssignmentRes> list(Long orgUnitId) {
        Long resolved = resolveOrgUnitId(orgUnitId);
        return assignmentRepository.findByOrgUnit_IdOrderByAssignedFromDesc(resolved).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MeterReaderAssignmentRes> listActive(Long orgUnitId) {
        Long resolved = resolveOrgUnitId(orgUnitId);
        return assignmentRepository.findByOrgUnit_IdOrderByAssignedFromDesc(resolved).stream()
                .map(this::toResponse)
                .filter(MeterReaderAssignmentRes::active)
                .toList();
    }

    @Transactional
    @Override
    public List<MeterReaderAssignmentRes> create(MeterReaderAssignmentCreateReq request) {
        Long resolved = resolveOrgUnitId(request.orgUnitId());
        if (request.readerUserId() == null) {
            throw new BadRequestException("Reader is required", "කියවන්නා අවශ්‍යයි");
        }
        if (request.billingZoneIds() == null || request.billingZoneIds().isEmpty()) {
            throw new BadRequestException("At least one billing zone is required", "අඩුම වශයෙන් එක් බිල්පත් කලාපයක් අවශ්‍යයි");
        }
        if (request.assignedFrom() == null) {
            throw new BadRequestException("Assigned from date is required", "පැවරීම ආරම්භ වන දිනය අවශ්‍යයි");
        }

        User reader = userRepository.findById(request.readerUserId())
                .orElseThrow(() -> new NotFoundException("Reader not found", "කියවන්නා සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        if (!Objects.equals(reader.getOrgUnit() != null ? reader.getOrgUnit().getId() : null, resolved)) {
            throw new BadRequestException("Reader must belong to the selected branch", "කියවන්නා තෝරාගත් ශාඛාවට අයත් විය යුතුය");
        }
        if (!isMeterAppReader(reader)) {
            throw new BadRequestException("Selected user is not a meter app reader", "තේරූ පරිශීලකයා මීටර් යෙදුමේ කියවන්නෙකු නොවේ");
        }

        Set<Long> zoneIds = new LinkedHashSet<>(request.billingZoneIds());
        List<MeterReaderZoneAssignment> saved = new ArrayList<>();
        for (Long zoneId : zoneIds) {
            BillingZone zone = billingZoneRepository.findById(zoneId)
                    .orElseThrow(() -> new NotFoundException("Billing zone not found", "බිල්පත් කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
            if (!Objects.equals(zone.getOrgUnitId(), resolved)) {
                throw new BadRequestException("Billing zone must belong to the selected branch", "බිල්පත් කලාපය තෝරාගත් ශාඛාවට අයත් විය යුතුය");
            }

            List<MeterReaderZoneAssignment> currentAssignments = assignmentRepository
                    .findByOrgUnit_IdAndBillingZoneIdAndAssignedToIsNull(resolved, zoneId);
            for (MeterReaderZoneAssignment current : currentAssignments) {
                if (current.getAssignedFrom() != null && current.getAssignedFrom().isAfter(request.assignedFrom())) {
                    throw new BadRequestException("Assigned from date cannot be before an active assignment start date",
                            "පැවරීම ආරම්භ වන දිනය පවතින පැවරීමක ආරම්භක දිනයට පෙර විය නොහැක");
                }
                current.setAssignedTo(request.assignedFrom().minusDays(1));
            }
            assignmentRepository.saveAll(currentAssignments);

            MeterReaderZoneAssignment assignment = new MeterReaderZoneAssignment();
            assignment.setOrgUnit(resolveOrgUnit(resolved));
            assignment.setReaderUser(reader);
            assignment.setBillingZoneId(zoneId);
            assignment.setAssignedFrom(request.assignedFrom());
            assignment.setAssignedTo(request.assignedTo());
            assignment.setNote(request.note());
            saved.add(assignmentRepository.save(assignment));
        }

        return list(resolved);
    }

    @Transactional
    @Override
    public MeterReaderAssignmentRes end(Long assignmentId) {
        MeterReaderZoneAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found", "Assignment not found", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(assignment.getOrgUnit().getId());
        if (assignment.getAssignedTo() != null && assignment.getAssignedTo().isBefore(LocalDate.now())) {
            throw new BadRequestException("Assignment is already closed", "Assignment is already closed");
        }
        assignment.setAssignedTo(LocalDate.now().minusDays(1));
        return toResponse(assignmentRepository.save(assignment));
    }

    private MeterReaderAssignmentRes toResponse(MeterReaderZoneAssignment assignment) {
        BillingZone zone = billingZoneRepository.findById(assignment.getBillingZoneId()).orElse(null);
        User reader = assignment.getReaderUser();
        LocalDate today = LocalDate.now();
        boolean active = !assignment.getAssignedFrom().isAfter(today)
                && (assignment.getAssignedTo() == null || !assignment.getAssignedTo().isBefore(today));
        return new MeterReaderAssignmentRes(
                assignment.getId(),
                assignment.getOrgUnit().getId(),
                reader.getId(),
                reader.getName(),
                reader.getUsername(),
                assignment.getBillingZoneId(),
                zone != null ? zone.getZoneName() : "Unknown",
                assignment.getAssignedFrom(),
                assignment.getAssignedTo(),
                assignment.getNote(),
                active);
    }

    private boolean isMeterAppReader(User user) {
        return userRoleRepository.findByIdUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .anyMatch(role -> role.getAppScope() == RoleAppScope.METER_APP);
    }

    private Long resolveOrgUnitId(Long orgUnitId) {
        Long resolved = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolved == null) {
            throw new BadRequestException("Organization unit is required", "ආයතනික ඒකකය අවශ්‍යයි");
        }
        accessService.enforceOrgUnitAccess(resolved);
        return resolved;
    }

    private OrgUnit resolveOrgUnit(Long orgUnitId) {
        return orgUnitRepository.findById(orgUnitId)
                .orElseThrow(() -> new NotFoundException("Org unit not found", "ආයතනික ඒකකය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
    }
}
