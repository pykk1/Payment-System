package com.montran.paymentsystem.controller;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Balance;
import com.montran.paymentsystem.service.AccountServiceInterface;
import com.montran.paymentsystem.service.BalanceServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class BalanceController {

    @Autowired
    BalanceServiceInterface balanceService;

    @Autowired
    AccountServiceInterface accountService;


    @RequestMapping("/balance/view")
    public String viewAccounts(Model model) {
        List<Account> accountList = accountService.findActiveAccounts();
        model.addAttribute("accountsList", accountList);
        return "balance/view-account-balances";
    }

    @RequestMapping("/balance/accountOverview")
    public String viewBalanceForAccount(Model modelAudit, Model last, Long id) {
        List<Balance> balanceList = balanceService.findBalances(id);
        modelAudit.addAttribute("auditList", balanceList);
        Balance balance = balanceService.findLastEntry(id);
        last.addAttribute("last", balance);

        return "balance/view-account-overview";
    }


    @RequestMapping("/balance/audit")
    public String viewAudit(Model modelAudit) {
        List<Balance> balanceList = balanceService.findAuditBalances();
        modelAudit.addAttribute("auditList", balanceList);
        return "balance/audit-balances";
    }
}
