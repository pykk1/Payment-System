package com.montran.paymentsystem.repository;

import com.montran.paymentsystem.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query(value = "SELECT * from accounts WHERE number =?1  ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Account> findAccountAudit(String number);

    Account findById(Long id);

    boolean existsByNumber(String number);

    Account findFirstByNumber(String number);

    Account findFirstByNumberOrderByTimestampDesc(String number);

    @Query(value = "SELECT DISTINCT ON (number) number,* FROM accounts WHERE status='ACTIVE' ORDER BY \"number\",\"timestamp\" DESC",
            nativeQuery = true)
    List<Account> findActiveAccounts();

    @Query(value = "SELECT DISTINCT ON (number) number,* FROM accounts " +
            "WHERE status='UNDER_UPDATE_APPROVAL' OR status='UNDER_ADD_APPROVAL' " +
            "OR status = 'UNDER_DELETE_APPROVAL' ORDER BY \"number\",\"timestamp\" DESC",
            nativeQuery = true)
    List<Account> findUnderApprovalAccounts();

    List<Account> findByNumber(String number);

    @Query(value = "SELECT * FROM accounts  WHERE status='ACTIVE' AND number=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    Account findFirstActive(String number);

    @Query(value = "SELECT * FROM accounts  WHERE status='ACTIVE_AUDIT' AND number=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    Account findFirstActiveAudit(String number);
}
