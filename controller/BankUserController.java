package com.montran.paymentsystem.controller;

import com.montran.paymentsystem.entity.BankUser;
import com.montran.paymentsystem.service.BankUserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("/user")
public class BankUserController {

    @Autowired
    BankUserServiceInterface bankUserService;


    @RequestMapping(path = {"/", "/login"})
    public String login() {
        return "login-page";
    }

    @RequestMapping("/home")
    public String homePage() {
        return "home-page";
    }

    @RequestMapping("/view/user")
    public String viewUsers(Model model) {
        List<BankUser> bankUserList = bankUserService.findFirstByStatus("ACTIVE");
        model.addAttribute("bankUsersList", bankUserList);
        return "user/view-user";
    }

    @RequestMapping("/add/users")
    public String addUsers(Model model) {
        BankUser bankUser = new BankUser();
        model.addAttribute("bankUser", bankUser);
        return "user/add-user";
    }

    @RequestMapping(value = "/add/user", method = RequestMethod.POST)
    public String saveUser(@ModelAttribute(value = "bankUser") BankUser bankUser) {
        bankUserService.addBankUser(bankUser);
        return "redirect:/view/user";

    }

    @RequestMapping("/approve/user")
    public String showUnderApprovalUsers(Model model) {
        List<BankUser> bankUserList = bankUserService.showUnderApprovalUsers();
        model.addAttribute("underApprovalBankUsersList", bankUserList);
        return "user/approve-user";
    }

    @PostMapping("/approve/user")
    @ResponseBody
    public String approveUser(Long id) {
        return bankUserService.approveBankUser(id);
    }

    @RequestMapping(value = "/view/user/updateToEdit", method = RequestMethod.POST)
    public String updateUser(@ModelAttribute(value = "bankUser") BankUser bankUser, Model model, Model modelAudit) {
        List<BankUser> auditList = bankUserService.findAllByUsername(bankUser.getUsername());
        modelAudit.addAttribute("audit", auditList);
        return "user/user-update";
    }

    @RequestMapping("/view/user/updateUser")
    public String updatedUser(BankUser bankUser) {
        bankUserService.updateBankUser(bankUser);

        return "redirect:/view/user";
    }

    @RequestMapping("/view/user/delete")
    public String deleteUser(Long id) {
        bankUserService.deleteBankUser(id);
        return "redirect:/view/user";
    }
}
