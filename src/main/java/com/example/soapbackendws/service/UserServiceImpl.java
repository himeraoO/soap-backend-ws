package com.example.soapbackendws.service;

import com.example.soapbackendws.repository.RoleRepository;
import com.example.soapbackendws.repository.UserRepository;
import com.example.soapbackendws.repository.entity.Role;
import com.example.soapbackendws.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    private final RoleRepository roledao;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roledao) {
        this.userRepository = userRepository;
        this.roledao = roledao;
    }

    @Override
    public void save(User user) {
        if(user.getRoles().isEmpty() || user.getRoles() == null){
            Set<Role> roleSet = new HashSet<>();
            roleSet.add(roledao.findById(2L).get());
            user.setRoles(roleSet);
        }
        userRepository.save(user);
    }

    @Override
    public void save(User user, List<Long> list) {
        Set<Role> roleSet = new HashSet<>();
        for (Long l:list) {
            roleSet.add(roledao.findById(l).get());
        }
        user.setRoles(roleSet);
        userRepository.save(user);
    }


    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void update(User user, Long... longs) {
        if (longs.length != 0) {
            save(user, Arrays.asList(longs));
        }else {
            save(user);
        }
    }

    @Override
    public void update(User user, List<Long> listRolesIds) {
        save(user, listRolesIds);
    }

    @Override
    public void update(User user) {
        save(user);
    }
}
