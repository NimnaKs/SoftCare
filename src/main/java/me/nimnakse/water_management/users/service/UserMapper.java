package me.nimnakse.water_management.users.service;

import me.nimnakse.water_management.common.config.CommonMapperConfig;
import me.nimnakse.water_management.users.dto.internal.UserMiniInternalDto;
import me.nimnakse.water_management.users.dto.response.UserRes;
import me.nimnakse.water_management.users.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "orgUnitId", source = "orgUnit.id")
    @Mapping(target = "agencyId", source = "agency.id")
    UserRes toUserRes(User user);

    @Mapping(target = "orgUnitId", source = "orgUnit.id")
    UserMiniInternalDto toMiniInternal(User user);
}
