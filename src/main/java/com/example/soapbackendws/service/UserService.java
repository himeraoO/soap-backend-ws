package com.example.soapbackendws.service;

import com.example.soapbackendws.repository.entity.User;

import java.util.List;

public interface UserService {
    void save(User user);

    void save(User user, List<Long> list);

    User findByLogin(String login);

    void delete (User user);

    List<User> findAll();

    void update(User user, Long... rolesIds);

    void update(User user, List<Long> listRolesIds);

    void update(User user);
}
