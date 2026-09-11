package com.chheang.mengheak.logisticsapis.mapper;

import com.chheang.mengheak.logisticsapis.dto.auth.response.UserAccountResponse;
import com.chheang.mengheak.logisticsapis.entities.auth.Role;
import com.chheang.mengheak.logisticsapis.entities.auth.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserAccountResponse toResponse(UserAccount user);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
