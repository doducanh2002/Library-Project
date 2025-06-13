package org.aibles.authenservice.service;

import org.aibles.authenservice.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    User getUserByEmail(String email);
    User createUser(String email);
}
