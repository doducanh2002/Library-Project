package org.aibles.authenservice.service.impl;

import org.aibles.authenservice.entity.Account;
import org.aibles.authenservice.exception.*;
import org.aibles.authenservice.repository.AccountRepository;
import org.aibles.authenservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Account createAccount(String username, String password, String userId, boolean activated, boolean isLocked) {
        log.info("Creating account with username: {} and userId: {}", username, userId);

        if (accountRepository.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new UsernameAlreadyExistsException();
        }
        if (password == null || password.length() < 8) {
            log.warn("Invalid password for username: {}", username);
            throw new PasswordInvalidException();
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setActivated(activated);
        account.setIsLocked(isLocked);
        account.setUserId(userId);
        Account savedAccount = accountRepository.save(account);
        log.debug("Successfully created account with ID: {} for username: {}", savedAccount.getId(), username);
        return savedAccount;

    }

    public Optional<Account> findByUsername(String username) {
        log.info("Finding account by username: {}", username);

        Optional<Account> account = accountRepository.findByUsername(username);
        log.debug("Account found for username: {}. Found: {}", username, account.isPresent());
        return account;

    }

    public Optional<Account> findByUserId(String userId) {
        log.info("Finding account by userId: {}", userId);

        Optional<Account> account = accountRepository.findByUserId(userId);
        log.debug("Account found for userId: {}. Found: {}", userId, account.isPresent());
        return account;

    }

    @Transactional
    public Account updatePassword(Account account, String newPassword) {
        log.info("Updating password for account with username: {}", account.getUsername());

        account.setPassword(passwordEncoder.encode(newPassword));
        Account updatedAccount = accountRepository.save(account);
        log.debug("Successfully updated password for account ID: {}", updatedAccount.getId());
        return updatedAccount;

    }

    @Transactional
    public Account activateAccount(Account account) {
        log.info("Activating account with username: {}", account.getUsername());

        account.setActivated(true);
        Account activatedAccount = accountRepository.save(account);
        log.debug("Successfully activated account ID: {}", activatedAccount.getId());
        return activatedAccount;

    }

    @Transactional
    public Account lockAccount(Account account) {
        log.info("Locking account with username: {}", account.getUsername());

        account.setIsLocked(false);
        Account lockedAccount = accountRepository.save(account);
        log.debug("Successfully locked account ID: {}", lockedAccount.getId());
        return lockedAccount;

    }
}