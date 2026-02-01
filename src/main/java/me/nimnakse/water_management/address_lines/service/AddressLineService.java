package me.nimnakse.water_management.address_lines.service;

import java.util.List;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineCreateReq;
import me.nimnakse.water_management.address_lines.dto.request.AddressLineUpdateReq;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineHierarchyRes;
import me.nimnakse.water_management.address_lines.dto.response.AddressLineRes;

public interface AddressLineService {
    AddressLineRes create(AddressLineCreateReq request);

    List<AddressLineRes> search(String query);

    AddressLineRes getById(Long id);

    AddressLineHierarchyRes getHierarchy(Long id);

    List<AddressLineHierarchyRes> getHierarchies();

    List<AddressLineRes> getAll();

    AddressLineRes update(Long id, AddressLineUpdateReq request);

    void delete(Long id);
}
