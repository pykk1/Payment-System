package com.montran.paymentsystem.repository;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Integer> {

    @Query(value = "SELECT DISTINCT ON (account_id) account_id,* FROM balances WHERE account_id=?1 ORDER BY \"account_id\",\"timestamp\" DESC",
            nativeQuery = true)
    Balance findFirstByAccountOrderByTimestampDesc(Long id);

    @Query(value = "SELECT * FROM balances WHERE account_id=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Balance> findBalancesOfAccount(Long id);

    Balance findFirstByAccount_IdOrderByTimestampDesc(Long id);

    @Query(value = "SELECT * FROM balances ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Balance> findAuditBalances();

    boolean existsByAccount(Account account);
}
