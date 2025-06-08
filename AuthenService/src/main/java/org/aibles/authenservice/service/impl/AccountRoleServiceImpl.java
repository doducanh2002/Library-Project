package org.aibles.authenservice.service.impl;

import org.aibles.authenservice.entity.AccountRole;
import org.aibles.authenservice.repository.AccountRoleRepository;
import org.aibles.authenservice.service.AccountRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountRoleServiceImpl implements AccountRoleService {

    private static final Logger log = LoggerFactory.getLogger(AccountRoleServiceImpl.class);

    @Autowired
    private AccountRoleRepository accountRoleRepository;

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
}