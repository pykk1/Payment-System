package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.BankUser;

import java.util.List;

public interface BankUserServiceInterface {
    List<BankUser> showUnderApprovalUsers();

    List<BankUser> findFirstByStatus(String status);

    String addBankUser(BankUser bankUser);

    String approveBankUser(Long id);

    String getLoggedInUser();

    String updateBankUser(BankUser bankUser);

    BankUser cloneBankUser(BankUser bankUser);

    String deleteBankUser(Long id);

    List<BankUser> findAllByUsername(String username);

}
