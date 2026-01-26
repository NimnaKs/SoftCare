package me.nimnakse.water_management.clusters.service;

import java.util.List;
import me.nimnakse.water_management.clusters.dto.request.ClusterCreateReq;
import me.nimnakse.water_management.clusters.dto.request.ClusterUpdateReq;
import me.nimnakse.water_management.clusters.dto.response.ClusterRes;

public interface ClusterService {
    ClusterRes create(ClusterCreateReq request);

    ClusterRes update(Long id, ClusterUpdateReq request);

    ClusterRes getById(Long id);

    List<ClusterRes> list(Long orgUnitId);

    void delete(Long id);
}
