package org.aibles.authenservice.service;

import org.aibles.authenservice.entity.Role;

public interface RoleService {
    Role findOrCreateRole(String roleName);
}
