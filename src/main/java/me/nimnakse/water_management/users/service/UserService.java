package me.nimnakse.water_management.users.service;

import me.nimnakse.water_management.users.dto.request.UserCreateReq;
import me.nimnakse.water_management.users.dto.request.UserUpdateReq;
import me.nimnakse.water_management.users.dto.response.UserRes;

public interface UserService {
    UserRes create(UserCreateReq request);

    UserRes update(Long id, UserUpdateReq request);

    UserRes getById(Long id);

    void deactivate(Long id);
}
