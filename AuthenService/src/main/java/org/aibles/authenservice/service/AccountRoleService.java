package org.aibles.authenservice.service;

import org.aibles.authenservice.entity.AccountRole;

public interface AccountRoleService {
    AccountRole assignRole(String accountId, String roleId);
}
