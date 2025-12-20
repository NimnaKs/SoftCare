package me.nimnakse.watermanagement.users.service;

import me.nimnakse.watermanagement.users.dto.request.UserCreateReq;
import me.nimnakse.watermanagement.users.dto.request.UserUpdateReq;
import me.nimnakse.watermanagement.users.dto.response.UserRes;

public interface UserService {
    UserRes create(UserCreateReq request);

    UserRes update(Long id, UserUpdateReq request);

    UserRes getById(Long id);

    void deactivate(Long id);
}
