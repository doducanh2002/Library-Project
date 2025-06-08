package org.aibles.authenservice.service.impl;

import org.aibles.authenservice.entity.Role;
import org.aibles.authenservice.repository.RoleRepository;
import org.aibles.authenservice.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public Role findOrCreateRole(String roleName) {
        log.info("Finding or creating role with name: {}", roleName);

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.debug("Role not found, creating new role with name: {}", roleName);
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
        log.debug("Role found or created with ID: {} and name: {}", role.getId(), roleName);
        return role;

    }
}