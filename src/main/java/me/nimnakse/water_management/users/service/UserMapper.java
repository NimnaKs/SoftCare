package me.nimnakse.water_management.users.service;

import me.nimnakse.water_management.common.config.MapperConfig;
import me.nimnakse.water_management.users.dto.internal.UserMiniInternalDto;
import me.nimnakse.water_management.users.dto.response.UserRes;
import me.nimnakse.water_management.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    UserRes toUserRes(User user);

    UserMiniInternalDto toMiniInternal(User user);
}
