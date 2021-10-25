package com.montran.paymentsystem.repository;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findPaymentById(Long id);

    @Query(value = "SELECT * FROM payments WHERE status='VERIFY' OR status='APPROVE' OR status='AUTHORIZE' ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Payment> findAllActivePayments();

    @Query(value = "SELECT * FROM payments WHERE status='VERIFY_AUDIT' OR status='APPROVE_AUDIT' OR status='AUTHORIZE_AUDIT' OR status='CANCELLED' ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Payment> findAuditPayments();

    @Query(value = "SELECT * FROM payments WHERE status='COMPLETED' ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Payment> findCompletedPayments();

    boolean existsByDebitAccount(Account account);

    @Query(value = "SELECT * FROM payments WHERE status='VERIFY' OR status='APPROVE' OR status='AUTHORIZE' AND credit_account=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Payment> findAllActivePaymentsByCreditAccount_Id(Long id);

    @Query(value = "SELECT * FROM payments WHERE status='VERIFY' OR status='APPROVE' OR status='AUTHORIZE' AND debit_account=?1 ORDER BY \"timestamp\" DESC",
            nativeQuery = true)
    List<Payment> findAllActivePaymentsByDebitAccount_Id(Long id);

    @Query(value = "SELECT status,COUNT(*) AS count,SUM(CAST(amount AS int)) AS amount FROM payments GROUP BY status",
            nativeQuery = true)
    List<String> findGeneralOverview();

}
