package me.nimnakse.water_management.address_lines.service;

import java.util.List;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;

public interface AddressLineService {
    AddressLineRes create(AddressLineCreateReq request);

    List<AddressLineRes> search(String query);

    AddressLineHierarchyRes getHierarchy(Long id);
}
