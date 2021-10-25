package com.montran.paymentsystem.controller;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.service.AccountServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AccountController {

    @Autowired
    AccountServiceInterface accountService;

    @RequestMapping("/view/account")
    public String viewAccounts(Model model) {
        List<Account> accountList = accountService.findActiveAccounts();
        model.addAttribute("accountsList", accountList);
        return "account/view-account";
    }

    @RequestMapping("/add/accounts")
    public String addAccount(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "account/add-account";
    }

    @RequestMapping(value = "/add/account", method = RequestMethod.POST)
    public String saveAccount(@ModelAttribute(value = "account") Account account) {
        accountService.addAccount(account);
        return "redirect:/view/account";
    }

    @RequestMapping("/approve/account")
    public String showUnderApprovalAccounts(Model model) {
        List<Account> accountList = accountService.findUnderApprovalAccounts();
        model.addAttribute("underApprovalAccounts", accountList);
        return "account/approve-account";
    }

    @PostMapping("approve/account")
    @ResponseBody
    public String approveAccount(Long id) {
        return accountService.approveAccount(id);
    }

    @RequestMapping(value = "/view/account/updateToEdit", method = RequestMethod.POST)
    public String updateAccount(@ModelAttribute(value = "account") Account account, Model model, Model modelAudit) {
        // model.addAttribute("accountToUpdate",account);
        modelAudit.addAttribute("audit", accountService.findAllByNumber(account.getNumber()));
        return "account/account-update";
    }

    @RequestMapping("/view/account/updateAccount")
    public String updatedAccount(Account account) {
        accountService.updateAccount(account);
        return "redirect:/view/account";
    }

    @RequestMapping("/view/account/delete")
    public String deleteAccount(Long id) {
        accountService.deleteAccount(id);
        return "redirect:/view/account";
    }
}
