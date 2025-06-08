package org.aibles.authenservice.service;

import org.aibles.authenservice.entity.Account;

public interface AccountService {

    Account createAccount(String username, String password, String userId, boolean activated, boolean isLocked);
    Account updatePassword(Account account, String newPassword);
    Account activateAccount(Account account);
    Account lockAccount(Account account);
}
