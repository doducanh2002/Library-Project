package org.aibles.authenservice.service;

import org.aibles.authenservice.entity.AccountRole;

import java.util.List;

public interface AccountRoleService {
    AccountRole assignRole(String accountId, String roleId);
    List<String> getRolesByAccountId(String accountId);
}
