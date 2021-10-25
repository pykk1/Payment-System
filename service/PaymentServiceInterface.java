package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Balance;
import com.montran.paymentsystem.entity.Payment;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PaymentServiceInterface {
    String addPayment(Payment payment);

    String getLoggedInUser();

    String verifyPayment(Long paymentId, String amount);

    String approvePayment(Long paymentId);

    String authorizePayment(Long paymentId);

    String cancelPayment(Long paymentId);

    Payment clonePayment(Payment payment);

    List<Payment> findActivePayments();

    Balance cloneBalance(Balance balance);

    String result(String a, String b);

    void saveData(Balance balanceCredit, Balance balanceDebit, Payment payment1, Payment paymentAudit, String auditStatus);

    void revokeData(Balance balanceCredit, Balance balanceDebit, Payment payment1, Payment paymentAudit, String auditStatus);

    List<Payment> findAuditPayments();

    List<Payment> findCompletedPayments();

    List<Payment> findGeneralOverview();

    boolean checkStatusOK(Account debitAccount, Account creditAccount);

    ResponseEntity<?> getPositions();

    ResponseEntity<?> sendExternalPayment(Payment payment);
}
