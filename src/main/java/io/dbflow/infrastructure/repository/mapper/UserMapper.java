package io.dbflow.infrastructure.repository.mapper;

import io.dbflow.domain.User;

public interface UserMapper {

    void deleteAll();

    void insert(User user);

    User findActiveUser();

    void updateCurrentDbConfigId(User user);

    void updateDelDbConfigId(User user);
}