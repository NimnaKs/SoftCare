package me.nimnakse.water_management.members.service;

import java.util.List;
import me.nimnakse.water_management.members.dto.request.MemberCreateReq;
import me.nimnakse.water_management.members.dto.request.MemberUpdateReq;
import me.nimnakse.water_management.members.dto.response.MemberRes;
import me.nimnakse.water_management.common.api.PageResponse;

public interface MemberService {
    MemberRes create(MemberCreateReq request);

    MemberRes update(Long id, MemberUpdateReq request);

    MemberRes getById(Long id);

    PageResponse<MemberRes> getPage(int page, int size);

    PageResponse<MemberRes> search(String membershipCode, String nicNumber, String registrationNumber, String mobileNumber, int page, int size);
}
