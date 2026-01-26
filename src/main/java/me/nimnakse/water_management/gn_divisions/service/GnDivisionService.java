package me.nimnakse.water_management.gn_divisions.service;

import java.util.List;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionCreateReq;
import me.nimnakse.water_management.gn_divisions.dto.request.GnDivisionUpdateReq;
import me.nimnakse.water_management.gn_divisions.dto.response.GnDivisionRes;

public interface GnDivisionService {
    GnDivisionRes create(GnDivisionCreateReq request);

    GnDivisionRes update(Long id, GnDivisionUpdateReq request);

    GnDivisionRes getById(Long id);

    List<GnDivisionRes> list(Long orgUnitId);

    void delete(Long id);
}
