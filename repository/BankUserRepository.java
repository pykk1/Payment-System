package com.montran.paymentsystem.repository;

import com.montran.paymentsystem.entity.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankUserRepository extends JpaRepository<BankUser, Integer> {

    List<BankUser> findBankUserByUsernameOrderByTimestampDesc(String username);

    BankUser findById(Long id);

    BankUser findFirstByUsernameOrderByTimestampDesc(String username);

    boolean existsByUsername(String username);

    @Query(value = "SELECT DISTINCT ON (username) username,* FROM bank_users WHERE status='UNDER_UPDATE_APPROVAL' OR status='UNDER_APPROVAL' " +
            "OR status = 'UNDER_DELETE_APPROVAL' ORDER BY \"username\",\"timestamp\" DESC",
            nativeQuery = true)
    List<BankUser> findAllByStatus(String status);

    @Query(value = "SELECT DISTINCT ON (username) username,* FROM bank_users WHERE status='ACTIVE' ORDER BY \"username\",\"timestamp\" DESC",
            nativeQuery = true)
    List<BankUser> findDistinctByStatus(String status);

    @Query(value = "SELECT * FROM bank_users " +
            "WHERE status='UNDER_UPDATE_APPROVAL' OR status='UNDER_ADD_APPROVAL' " +
            "OR status = 'UNDER_DELETE_APPROVAL' ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<BankUser> findUnderApprovalUsers();

    @Query(value = "SELECT * FROM bank_users  WHERE status='ACTIVE' AND username=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    BankUser findFirstActive(String username);


}
