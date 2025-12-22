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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClusterServiceImpl implements ClusterService {
    private final ClusterRepository clusterRepository;
    private final ConnectionRepository connectionRepository;

    public ClusterServiceImpl(ClusterRepository clusterRepository,
                              ConnectionRepository connectionRepository) {
        this.clusterRepository = clusterRepository;
        this.connectionRepository = connectionRepository;
    }

    @Transactional
    @Override
    public ClusterRes create(ClusterCreateReq request) {
        if (clusterRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Cluster already exists");
        }
        Cluster cluster = new Cluster();
        cluster.setName(request.name().trim());
        return toResponse(clusterRepository.save(cluster));
    }

    @Transactional
    @Override
    public ClusterRes update(Long id, ClusterUpdateReq request) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cluster not found", ErrorCode.NOT_FOUND));
        if (clusterRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new BadRequestException("Cluster already exists");
        }
        cluster.setName(request.name().trim());
        return toResponse(clusterRepository.save(cluster));
    }

    @Transactional(readOnly = true)
    @Override
    public ClusterRes getById(Long id) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cluster not found", ErrorCode.NOT_FOUND));
        return toResponse(cluster);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ClusterRes> list() {
        return clusterRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Cluster cluster = clusterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cluster not found", ErrorCode.NOT_FOUND));
        if (connectionRepository.existsByClusterId(id)) {
            throw new BadRequestException("Cluster is linked to connections and cannot be deleted");
        }
        clusterRepository.delete(cluster);
    }

    private ClusterRes toResponse(Cluster cluster) {
        return new ClusterRes(
                cluster.getId(),
                cluster.getName(),
                cluster.getCreatedAt(),
                cluster.getUpdatedAt()
        );
    }
}
