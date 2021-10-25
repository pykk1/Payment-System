package com.montran.paymentsystem.controller;

import com.montran.paymentsystem.entity.Payment;
import com.montran.paymentsystem.service.PaymentServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    PaymentServiceInterface paymentService;

    @RequestMapping("/payment/new")
    public String addPayment(Model model) {
        Payment payment = new Payment();
        model.addAttribute("payment", payment);
        return "payment/add-payment";
    }

    @GetMapping("/payment/ips/positions")
    public String getPositions(Model model) {
        ResponseEntity<?>  responseEntity = paymentService.getPositions();
        model.addAttribute("response",responseEntity);
        return "payment/get-positions";
    }

    @RequestMapping(value = "/payment/pay", method = RequestMethod.POST)
    @ResponseBody
    public String pay(@ModelAttribute(value = "payment") Payment payment) {
        return paymentService.addPayment(payment);
    }

    @RequestMapping(value = "/payment/view")
    public String viewPayments(Model model) {
        List<Payment> paymentList = paymentService.findActivePayments();
        model.addAttribute("paymentsList", paymentList);
        return "payment/view-payment";
    }

    @PostMapping(value = "/payment/verify")
    @ResponseBody
    public String verifyPayment(Long id, String enteredAmount) {
        return paymentService.verifyPayment(id, enteredAmount);
    }

    @PostMapping("/payment/approve")
    @ResponseBody
    public String approvePayment(Long id) {
        return paymentService.approvePayment(id);
    }

    @PostMapping("/payment/authorize")
    @ResponseBody
    public String authorizePayment(Long id) {
        return paymentService.authorizePayment(id);
    }

    @PostMapping("/payment/cancel")
    @ResponseBody
    public String cancelPayment(Long id) {
        return paymentService.cancelPayment(id);
    }

    @RequestMapping(value = "/payment/audit")
    public String viewAudit(Model model) {
        List<Payment> paymentList = paymentService.findAuditPayments();
        model.addAttribute("auditList", paymentList);
        return "payment/audit-payment";
    }

    @RequestMapping(value = "/payment/completed")
    public String viewCompleted(Model model) {
        List<Payment> paymentList = paymentService.findCompletedPayments();
        model.addAttribute("auditList", paymentList);
        return "payment/audit-payment";
    }

    @RequestMapping("/payment/overview")
    public String viewGeneralOverview(Model model) {
        List<Payment> paymentList = paymentService.findGeneralOverview();
        model.addAttribute("paymentList", paymentList);

        return "payment/general-overview";
    }

    @RequestMapping(value = "/payment/ips",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String sendExternalPayment(@ModelAttribute(value = "payment") Payment payment){
        paymentService.sendExternalPayment(payment);
        return "Success !";
    }

    @RequestMapping("/payment/ips/new")
    public String newExternalPayment(Model model){
        Payment payment = new Payment();
        model.addAttribute("payment",payment);
        return "payment/add-external-payment";
    }
}
