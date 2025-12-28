package me.nimnakse.water_management.users.service;

import me.nimnakse.water_management.users.dto.request.UserCreateReq;
import me.nimnakse.water_management.users.dto.request.UserPasswordUpdateReq;
import me.nimnakse.water_management.users.dto.request.UserUpdateReq;
import me.nimnakse.water_management.common.api.PageResponse;
import me.nimnakse.water_management.users.dto.response.UserRes;
import me.nimnakse.water_management.users.entity.UserStatus;

import java.util.List;

public interface UserService {
    UserRes create(UserCreateReq request);

    UserRes update(Long id, UserUpdateReq request);

    UserRes getById(Long id);

    void deactivate(Long id);

    void activate(Long id);

    void updatePassword(Long id, UserPasswordUpdateReq request);

    List<UserRes> list(Long orgUnitId, UserStatus status);

    PageResponse<UserRes> listPaged(Long orgUnitId, UserStatus status, int page, int size);
}
