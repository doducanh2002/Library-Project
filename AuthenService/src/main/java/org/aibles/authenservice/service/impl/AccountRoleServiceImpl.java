package org.aibles.authenservice.service.impl;

import org.aibles.authenservice.entity.AccountRole;
import org.aibles.authenservice.repository.AccountRoleRepository;
import org.aibles.authenservice.repository.RoleRepository;
import org.aibles.authenservice.service.AccountRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountRoleServiceImpl implements AccountRoleService {

    private static final Logger log = LoggerFactory.getLogger(AccountRoleServiceImpl.class);

    @Autowired
    private AccountRoleRepository accountRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public AccountRole assignRole(String accountId, String roleId) {
        log.info("Assigning role with roleId: {} to accountId: {}", roleId, accountId);
        AccountRole accountRole = new AccountRole();
        accountRole.setAccountId(accountId);
        accountRole.setRoleId(roleId);
        AccountRole savedAccountRole = accountRoleRepository.save(accountRole);
        log.debug("Successfully assigned role with roleId: {} to accountId: {}", roleId, accountId);
        return savedAccountRole;
    }
    
    @Override
    public List<String> getRolesByAccountId(String accountId) {
        log.info("Getting roles for accountId: {}", accountId);
        List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(accountId);
        List<String> roleNames = accountRoles.stream()
                .map(accountRole -> {
                    String roleName = roleRepository.findById(accountRole.getRoleId())
                            .map(role -> role.getName())
                            .orElse("UNKNOWN");
                    log.debug("Found role: {} for accountId: {}", roleName, accountId);
                    return roleName;
                })
                .collect(Collectors.toList());
        log.info("User with accountId: {} has roles: {}", accountId, roleNames);
        return roleNames;
    }
}