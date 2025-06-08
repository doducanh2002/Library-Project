package org.aibles.authenservice.repository;

import org.aibles.authenservice.entity.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, String> {

    List<AccountRole> findByAccountId(String accountId);
    boolean existsByAccountIdAndRoleId(String accountId, String roleId);
    void deleteByAccountId(String accountId);
}