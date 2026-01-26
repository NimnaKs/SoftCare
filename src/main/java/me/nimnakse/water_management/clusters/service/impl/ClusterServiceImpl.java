package me.nimnakse.water_management.clusters.service.impl;

import java.util.List;
import me.nimnakse.water_management.clusters.dto.request.ClusterCreateReq;
import me.nimnakse.water_management.clusters.dto.request.ClusterUpdateReq;
import me.nimnakse.water_management.clusters.dto.response.ClusterRes;
import me.nimnakse.water_management.clusters.entity.Cluster;
import me.nimnakse.water_management.clusters.repository.ClusterRepository;
import me.nimnakse.water_management.clusters.service.ClusterService;
import me.nimnakse.water_management.common.exception.BadRequestException;
import me.nimnakse.water_management.common.exception.ErrorCode;
import me.nimnakse.water_management.common.exception.NotFoundException;
import me.nimnakse.water_management.connections.repository.ConnectionRepository;
import me.nimnakse.water_management.security.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClusterServiceImpl implements ClusterService {
    private final ClusterRepository clusterRepository;
    private final ConnectionRepository connectionRepository;
    private final OrganizationAccessService accessService;

    public ClusterServiceImpl(ClusterRepository clusterRepository,
            ConnectionRepository connectionRepository,
            OrganizationAccessService accessService) {
        this.clusterRepository = clusterRepository;
        this.connectionRepository = connectionRepository;
        this.accessService = accessService;
    }

    @Transactional
    @Override
    public ClusterRes create(ClusterCreateReq request) {
        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : accessService.resolveOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (clusterRepository.existsByOrgUnitIdAndNameIgnoreCase(orgUnitId, request.name())) {
            throw new BadRequestException("Cluster already exists", "කලාපය දැනටමත් පවතී");
        }
        Cluster cluster = new Cluster();
        cluster.setOrgUnitId(orgUnitId);
        cluster.setName(request.name().trim());
        return toResponse(clusterRepository.save(cluster));
    }

    @Transactional
    @Override
    public ClusterRes update(Long id, ClusterUpdateReq request) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Cluster not found", "කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND));

        Long orgUnitId = request.orgUnitId() != null ? request.orgUnitId() : cluster.getOrgUnitId();
        accessService.enforceOrgUnitAccess(orgUnitId);

        if (clusterRepository.existsByOrgUnitIdAndNameIgnoreCaseAndIdNot(orgUnitId, request.name(), id)) {
            throw new BadRequestException("Cluster already exists", "කලාපය දැනටමත් පවතී");
        }
        cluster.setOrgUnitId(orgUnitId);
        cluster.setName(request.name().trim());
        return toResponse(clusterRepository.save(cluster));
    }

    @Transactional(readOnly = true)
    @Override
    public ClusterRes getById(Long id) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Cluster not found", "කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(cluster.getOrgUnitId());
        return toResponse(cluster);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClusterRes> list(Long orgUnitId) {
        Long resolvedOrgUnitId = orgUnitId != null ? orgUnitId : accessService.resolveOrgUnitId();
        if (resolvedOrgUnitId != null) {
            accessService.enforceOrgUnitAccess(resolvedOrgUnitId);
            return clusterRepository.findByOrgUnitId(resolvedOrgUnitId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return clusterRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Cluster not found", "කලාපය සොයාගත නොහැක", ErrorCode.NOT_FOUND));
        accessService.enforceOrgUnitAccess(cluster.getOrgUnitId());
        if (connectionRepository.existsByClusterId(id)) {
            throw new BadRequestException("Cluster is linked to connections and cannot be deleted",
                    "කලාපය සම්බන්ධතාවලට සම්බන්ධ කර ඇති බැවින් මකා දැමිය නොහැක");
        }
        clusterRepository.delete(cluster);
    }

    private ClusterRes toResponse(Cluster cluster) {
        return new ClusterRes(
                cluster.getId(),
                cluster.getName(),
                cluster.getOrgUnitId(),
                cluster.getCreatedAt(),
                cluster.getUpdatedAt());
    }
}
