package me.nimnakse.water_management.agencies.service;

import java.util.List;
import me.nimnakse.water_management.agencies.dto.request.AgencyCreateReq;
import me.nimnakse.water_management.agencies.dto.request.AgencyUpdateReq;
import me.nimnakse.water_management.agencies.dto.response.AgencyRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface AgencyService {
    AgencyRes create(AgencyCreateReq request);

    AgencyRes update(Long id, AgencyUpdateReq request);

    AgencyRes getById(Long id);

    List<AgencyRes> list();

    PageResponse<AgencyRes> listPaginated(int page, int size);

    void delete(Long id);
}
