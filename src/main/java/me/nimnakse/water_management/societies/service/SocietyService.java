package me.nimnakse.water_management.societies.service;

import java.util.List;
import me.nimnakse.water_management.societies.dto.request.SocietyCreateReq;
import me.nimnakse.water_management.societies.dto.request.SocietyUpdateReq;
import me.nimnakse.water_management.societies.dto.response.SocietyRes;

public interface SocietyService {
    SocietyRes create(SocietyCreateReq request);

    SocietyRes update(Long id, SocietyUpdateReq request);

    SocietyRes getById(Long id);

    List<SocietyRes> list();

    void delete(Long id);
}
