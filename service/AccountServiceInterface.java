package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Account;

import java.util.List;

public interface AccountServiceInterface {
    List<Account> findActiveAccounts();

    Account addAccount(Account account);

    String approveAccount(Long id);

    String getLoggedInUser();

    Account cloneAccount(Account account);

    Account updateAccount(Account account);

    String deleteAccount(Long id);

    List<Account> findUnderApprovalAccounts();

    List<Account> findAllByNumber(String number);

    void assignBalance(Account oldAccount, Account newAccount);
}
