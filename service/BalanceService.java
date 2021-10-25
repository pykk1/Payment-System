package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Balance;
import com.montran.paymentsystem.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class BalanceService implements BalanceServiceInterface {

    @Autowired
    BalanceRepository balanceRepository;

    @Override
    public List<Balance> findBalances(Long id) {
        return balanceRepository.findBalancesOfAccount(id);
    }

    @Override
    public Balance findLastEntry(Long id) {
        return balanceRepository.findFirstByAccount_IdOrderByTimestampDesc(id);
    }

    @Override
    public List<Balance> findAuditBalances() {
        return balanceRepository.findAuditBalances();
    }
}
