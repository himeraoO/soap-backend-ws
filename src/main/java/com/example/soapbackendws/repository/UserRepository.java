package com.example.soapbackendws.repository;

import com.example.soapbackendws.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByLogin(String login);
}
