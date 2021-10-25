package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Balance;

import java.util.List;

public interface BalanceServiceInterface {
    List<Balance> findBalances(Long id);

    Balance findLastEntry(Long id);

    List<Balance> findAuditBalances();
}
