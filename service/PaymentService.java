package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Balance;
import com.montran.paymentsystem.entity.Payment;
import com.montran.paymentsystem.repository.AccountRepository;
import com.montran.paymentsystem.repository.BalanceRepository;
import com.montran.paymentsystem.repository.PaymentRepository;
import com.montran.paymentsystem.status.AccountStatus;
import com.montran.paymentsystem.status.Currency;
import com.montran.paymentsystem.status.PaymentStatus;
import com.montran.paymentsystem.status.PaymentType;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class PaymentService implements PaymentServiceInterface {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    BalanceRepository balanceRepository;

    @Override
    public String addPayment(Payment payment) {
        payment.setDebitAccount(accountRepository.findFirstByNumberOrderByTimestampDesc(payment.getDebitAccountNumber()));
        payment.setCreditAccount(accountRepository.findFirstByNumberOrderByTimestampDesc(payment.getCreditAccountNumber()));
        payment.setCurrency(Currency.IDR);

        if (!checkStatusOK(payment.getDebitAccount(), payment.getCreditAccount()))
            return "At least one of the accounts is unable to make a transaction";

        if (!payment.getDebitAccount().getCurrency().equals(payment.getCreditAccount().getCurrency()))
            return "Currencies don't match";

        payment.setUsername(getLoggedInUser());
        payment.setStatus(PaymentStatus.VERIFY);
        payment.setType(PaymentType.INTERNAL);

        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment.getDebitAccount().getId()));
        Balance balanceCredit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment.getCreditAccount().getId()));

        balanceDebit.setPendingDebit(result(balanceDebit.getPendingDebit(), "1"));
        balanceDebit.setAmountDebit(result(balanceDebit.getAmountDebit(), payment.getAmount()));
        balanceDebit.setAvailable(result(balanceDebit.getAvailable(), "-" + payment.getAmount()));
        balanceDebit.setProjected(result(balanceDebit.getProjected(), "-" + payment.getAmount()));

        balanceCredit.setPendingCredit(result(balanceCredit.getPendingCredit(), "1"));
        balanceCredit.setAmountCredit(result(balanceCredit.getAmountCredit(), payment.getAmount()));
        balanceCredit.setProjected(result(balanceCredit.getProjected(), payment.getAmount()));

        balanceRepository.save(balanceCredit);
        balanceRepository.save(balanceDebit);
        paymentRepository.save(payment);
        return "Success !";
    }

    @Override
    public String getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public String verifyPayment(Long paymentId, String amount) {

        Payment paymentAudit = paymentRepository.findPaymentById(paymentId);
        Payment payment1 = clonePayment(paymentAudit);
        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getDebitAccount().getId()));
        Balance balanceCredit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getCreditAccount().getId()));

        if (!checkStatusOK(payment1.getDebitAccount(), payment1.getCreditAccount()))
            return "At least one of the accounts is unable to make a transaction";

        if (!payment1.getStatus().equals(PaymentStatus.VERIFY)) return "Can't be verified";

        if (payment1.getUsername().equals(getLoggedInUser())) return "You cannot approve this payment !";

        if (payment1.getAmount().equals(amount)) {

            payment1.setUsername(getLoggedInUser());

            if (Long.parseLong(payment1.getAmount()) > 1000L) {

                payment1.setStatus(PaymentStatus.APPROVE);
                paymentRepository.save(payment1);

                paymentAudit.setStatus(PaymentStatus.VERIFY_AUDIT);
                paymentRepository.save(paymentAudit);
                return "Success, sent for approval !";

            } else {

                saveData(balanceCredit, balanceDebit, payment1, paymentAudit, PaymentStatus.VERIFY_AUDIT);
                return "Success !";

            }

        }
        return "Wrong amount !";
    }

    @Override
    public String approvePayment(Long paymentId) {

        Payment paymentAudit = paymentRepository.findPaymentById(paymentId);
        Payment payment1 = clonePayment(paymentAudit);
        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getDebitAccount().getId()));
        Balance balanceCredit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getCreditAccount().getId()));

        if (!checkStatusOK(payment1.getDebitAccount(), payment1.getCreditAccount()))
            return "At least one of the accounts is unable to make a transaction";

        if (!payment1.getStatus().equals(PaymentStatus.APPROVE)) return "Can't be approved";

        if (payment1.getUsername().equals(getLoggedInUser())) return "You cannot approve this payment !";

        if (Long.parseLong(result(balanceDebit.getCredit(), "-" + balanceDebit.getDebit())) < Long.parseLong(payment1.getAmount())) {
            payment1.setStatus(PaymentStatus.AUTHORIZE);
            paymentRepository.save(payment1);

            paymentAudit.setStatus(PaymentStatus.APPROVE_AUDIT);
            paymentRepository.save(paymentAudit);
            return "Success, sent for authorization !";
        }

        payment1.setUsername(getLoggedInUser());

        saveData(balanceCredit, balanceDebit, payment1, paymentAudit, PaymentStatus.APPROVE_AUDIT);
        return "Success !";
    }

    @Override
    public String authorizePayment(Long paymentId) {

        Payment paymentAudit = paymentRepository.findPaymentById(paymentId);
        Payment payment1 = clonePayment(paymentAudit);
        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getDebitAccount().getId()));
        Balance balanceCredit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getCreditAccount().getId()));

        if (!checkStatusOK(payment1.getDebitAccount(), payment1.getCreditAccount()))
            return "At least one of the accounts is unable to make a transaction";

        if (!payment1.getStatus().equals(PaymentStatus.AUTHORIZE)) return "Can't be authorized";

        if (payment1.getUsername().equals(getLoggedInUser())) return "You cannot authorize this payment !";

        payment1.setUsername(getLoggedInUser());

        saveData(balanceCredit, balanceDebit, payment1, paymentAudit, PaymentStatus.AUTHORIZE_AUDIT);
        return "Success !";
    }

    @Override
    public String cancelPayment(Long paymentId) {
        Payment paymentAudit = paymentRepository.findPaymentById(paymentId);
        Payment payment1 = clonePayment(paymentAudit);
        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getDebitAccount().getId()));
        Balance balanceCredit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment1.getCreditAccount().getId()));

        revokeData(balanceCredit, balanceDebit, payment1, paymentAudit, PaymentStatus.CANCELLED);
        return "Success !";
    }

    @Override
    public Payment clonePayment(Payment payment) {

        Payment payment1 = new Payment();
        payment1.setDebitAccount(payment.getDebitAccount());
        payment1.setCreditAccount(payment.getCreditAccount());
        payment1.setUsername(payment.getUsername());
        payment1.setCurrency(payment.getCurrency());
        payment1.setAmount(payment.getAmount());
        payment1.setCreditAccountNumber(payment.getCreditAccountNumber());
        payment1.setDebitAccountNumber(payment.getDebitAccountNumber());
        payment1.setReference(payment.getReference());
        payment1.setStatus(payment.getStatus());
        payment1.setType(payment.getType());
        return payment1;
    }

    @Override
    public List<Payment> findActivePayments() {
        return paymentRepository.findAllActivePayments();
    }

    @Override
    public Balance cloneBalance(Balance balance) {
        Balance balance1 = new Balance();
        balance1.setCredit(balance.getCredit());
        balance1.setAccount(balance.getAccount());
        balance1.setDebit(balance.getDebit());
        balance1.setAmountDebit(balance.getAmountDebit());
        balance1.setAmountCredit(balance.getAmountCredit());
        balance1.setAvailable(balance.getAvailable());
        balance1.setCountDebit(balance.getCountDebit());
        balance1.setCountCredit(balance.getCountCredit());
        balance1.setPendingDebit(balance.getPendingDebit());
        balance1.setPendingCredit(balance.getPendingCredit());
        balance1.setProjected(balance.getProjected());

        return balance1;
    }

    @Override
    public String result(String a, String b) {
        return String.valueOf(Long.parseLong(a) + Long.parseLong(b));
    }

    @Override
    public void saveData(Balance balanceCredit, Balance balanceDebit, Payment payment1, Payment paymentAudit, String auditStatus) {

        balanceDebit.setPendingDebit(result(balanceDebit.getPendingDebit(), "-1"));
        balanceDebit.setAmountDebit(result(balanceDebit.getAmountDebit(), "-" + payment1.getAmount()));
        balanceDebit.setCountDebit(result(balanceDebit.getCountDebit(), "1"));
        balanceDebit.setDebit(result(balanceDebit.getDebit(), payment1.getAmount()));

        balanceCredit.setPendingCredit(result(balanceCredit.getPendingCredit(), "-1"));
        balanceCredit.setAmountCredit(result(balanceCredit.getAmountCredit(), "-" + payment1.getAmount()));
        balanceCredit.setCountCredit(result(balanceCredit.getCountCredit(), "1"));
        balanceCredit.setCredit(result(balanceCredit.getCredit(), payment1.getAmount()));
        balanceCredit.setAvailable(result(balanceCredit.getAvailable(), payment1.getAmount()));

        payment1.setStatus(PaymentStatus.COMPLETED);
        balanceRepository.save(balanceCredit);
        balanceRepository.save(balanceDebit);
        paymentRepository.save(payment1);

        paymentAudit.setStatus(auditStatus);
        paymentRepository.save(paymentAudit);

    }

    @Override
    public void revokeData(Balance balanceCredit, Balance balanceDebit, Payment payment1, Payment paymentAudit, String auditStatus) {

        balanceDebit.setAvailable(result(balanceDebit.getAvailable(), payment1.getAmount()));
        balanceDebit.setPendingDebit(result(balanceDebit.getPendingDebit(), "-1"));
        balanceDebit.setAmountDebit(result(balanceDebit.getAmountDebit(), "-" + payment1.getAmount()));
        balanceDebit.setProjected(result(balanceDebit.getAvailable(), "-" + balanceDebit.getAmountCredit()));

        balanceCredit.setAmountCredit(result(balanceCredit.getAmountCredit(), "-" + payment1.getAmount()));
        balanceCredit.setPendingCredit(result(balanceCredit.getPendingCredit(), "-1"));
        balanceCredit.setProjected(result(balanceCredit.getAvailable(), "-" + balanceCredit.getAmountCredit()));

        //        balanceDebit.setDebit(result("-" + payment1.getAmount(), balanceDebit.getDebit()));
//        balanceDebit.setCredit(result(balanceDebit.getCredit(),"-"+payment1.getAmount()));
//        balanceDebit.setPendingDebit(result(balanceDebit.getPendingDebit(), "-" + payment1.getAmount()));
//        balanceDebit.setCountDebit(result(balanceDebit.getCountDebit(), "-1"));
//        balanceDebit.setAmountDebit(result(balanceDebit.getAmountDebit(), "-" + payment1.getAmount()));
//        balanceDebit.setAvailable(result(balanceDebit.getCredit(), "-" +balanceDebit.getDebit()));
//        balanceDebit.setProjected(result(result(balanceDebit.getAvailable(),balanceDebit.getPendingCredit()),"-"+balanceDebit.getPendingDebit()));
//
//        balanceCredit.setCredit(result(payment1.getAmount(), balanceCredit.getCredit()));
//        balanceCredit.setPendingCredit(result("-" + payment1.getAmount(), balanceCredit.getPendingCredit()));
//        balanceCredit.setCountCredit(result(balanceCredit.getCountCredit(), "-1"));
//        balanceCredit.setAmountCredit(result(balanceCredit.getAmountCredit(), "-" + payment1.getAmount()));
//        balanceCredit.setAvailable(result(balanceCredit.getCredit(), balanceCredit.getDebit()));
//        balanceCredit.setProjected(result(result(balanceCredit.getAvailable(),balanceCredit.getPendingCredit()),"-"+balanceCredit.getPendingDebit()));


        payment1.setStatus(PaymentStatus.CANCELLED);
        balanceRepository.save(balanceCredit);
        balanceRepository.save(balanceDebit);

        paymentAudit.setStatus(auditStatus);
        paymentRepository.save(paymentAudit);
    }

    @Override
    public List<Payment> findAuditPayments() {
        return paymentRepository.findAuditPayments();
    }

    @Override
    public List<Payment> findCompletedPayments() {
        return paymentRepository.findCompletedPayments();
    }

    @Override
    public List<Payment> findGeneralOverview() {
        List<String> list = paymentRepository.findGeneralOverview();
        Payment payment = new Payment();
        List<Payment> paymentList = new ArrayList<>();
        for (String str : list) {
            List<String> list1 = Arrays.asList(str.split(","));
            payment.setStatus(list1.get(0));
            payment.setCurrency(list1.get(1));
            payment.setAmount(list1.get(2));
            paymentList.add(payment);
            payment = new Payment();
        }
        return paymentList;
    }

    @Override
    public boolean checkStatusOK(Account debitAccount, Account creditAccount) {
        if (!(debitAccount.getAccountStatus().equals(AccountStatus.OPEN) && !debitAccount.getAccountStatus().equals(AccountStatus.BLOCK_DEBIT)))
            return false;
        if (!(creditAccount.getAccountStatus().equals(AccountStatus.OPEN) && !creditAccount.getAccountStatus().equals(AccountStatus.BLOCK_CREDIT)))
            return false;
        return true;
    }

    @Override
    public ResponseEntity<?> getPositions() {

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://ipsdemo.montran.ro/rtp/Positions";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-MONTRAN-RTP-Channel","INTAROB0");
        httpHeaders.set("X-MONTRAN-RTP-Version","1");

        ResponseEntity<?> responseEntity = restTemplate.exchange(url, HttpMethod.GET,new HttpEntity<>(httpHeaders),String.class);


        return ResponseEntity.ok(responseEntity.getBody());
    }

    @SneakyThrows
    @Override
    public ResponseEntity<?> sendExternalPayment(Payment payment) {
        Balance balanceDebit = cloneBalance(balanceRepository.findFirstByAccountOrderByTimestampDesc(payment.getDebitAccount().getId()));
        balanceDebit.setCountDebit(result(balanceDebit.getCountDebit(), "1"));
        balanceDebit.setDebit(result(balanceDebit.getDebit(), payment.getAmount()));
        balanceDebit.setAvailable(result(balanceDebit.getAvailable(),"-"+payment.getAmount()));
        balanceDebit.setProjected(result(balanceDebit.getProjected(), "-" + payment.getAmount()));

        balanceRepository.save(balanceDebit);
        paymentRepository.save(payment);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://ipsdemo.montran.ro/rtp/Message";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-MONTRAN-RTP-Channel","INTAROB0");
        httpHeaders.set("X-MONTRAN-RTP-Version","1");
        httpHeaders.set("Content-Type","text/xml");

        InputStream inputStream = new FileInputStream("src/main/resources/pacs.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while((line = reader.readLine()) != null){
            stringBuilder.append(line);
        }
        String pacs = stringBuilder.toString();

        pacs = pacs.replace("$REF$", UUID.randomUUID().toString().replaceAll("-", "").substring(0,28)+payment.getReference().charAt(0));
        pacs = pacs.replace("$DATETIME$", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()));
        pacs = pacs.replace("$AMOUNT$",payment.getAmount());
        pacs = pacs.replace("$SENDER$","INTAROB0");
        pacs = pacs.replace("$DATE$", LocalDate.now().toString());
        pacs = pacs.replace("$DEBTOR NAME$","Pavel");
        pacs = pacs.replace("$FROM_ACCOUNT$",payment.getDebitAccountNumber());
        pacs = pacs.replace("$RECEIVER$", payment.getExternalBIC());
        pacs = pacs.replace("$CREDITOR NAME$","test");
        pacs = pacs.replace("$TO ACCOUNT$",payment.getCreditAccountNumber());
        pacs = pacs.replace("$DETAILS$",payment.getReference());

        ResponseEntity<?> responseEntity = restTemplate.exchange(url, HttpMethod.POST,new HttpEntity<>(pacs,httpHeaders),String.class);
        System.out.println(responseEntity.getBody());
        return responseEntity;

    }
}
