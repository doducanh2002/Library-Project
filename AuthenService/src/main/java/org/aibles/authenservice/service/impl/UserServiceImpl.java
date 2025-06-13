package org.aibles.authenservice.service.impl;

import org.aibles.authenservice.entity.User;
import org.aibles.authenservice.exception.EmailAlreadyExistsException;
import org.aibles.authenservice.exception.UserNotFoundException;
import org.aibles.authenservice.repository.UserRepository;
import org.aibles.authenservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User createUser(String email) {
        log.info("Creating user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new EmailAlreadyExistsException();
        }
        User user = new User();
        user.setEmail(email);
        User savedUser = userRepository.save(user);
        log.debug("Successfully created user with ID: {} and email: {}", savedUser.getId(), email);
        return savedUser;

    }

    public Optional<User> findByEmail(String email) {
        log.info("Finding user by email: {}", email);

        Optional<User> user = userRepository.findByEmail(email);
        log.debug("User found for email: {}. Found: {}", email, user.isPresent());
        return user;

    }

    public User getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new UserNotFoundException(email);
                });
        log.debug("Successfully retrieved user with ID: {} for email: {}", user.getId(), email);
        return user;

    }
}