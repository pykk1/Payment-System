package com.montran.paymentsystem.service;

import com.montran.paymentsystem.entity.Account;
import com.montran.paymentsystem.entity.Balance;
import com.montran.paymentsystem.entity.Payment;
import com.montran.paymentsystem.repository.AccountRepository;
import com.montran.paymentsystem.repository.BalanceRepository;
import com.montran.paymentsystem.repository.PaymentRepository;
import com.montran.paymentsystem.status.AccountStatus;
import com.montran.paymentsystem.status.Currency;
import com.montran.paymentsystem.status.UserAndAccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
public class AccountService implements AccountServiceInterface {

    @Autowired
    private AccountRepository repository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Override
    public List<Account> findActiveAccounts() {
        return repository.findActiveAccounts();
    }

    @Override
    public Account addAccount(Account account) {
        if (repository.existsByNumber(account.getNumber()))
            return null;
        account.setModified_by(getLoggedInUser());
        account.setStatus(UserAndAccountStatus.UNDER_ADD_APPROVAL);
        account.setCurrency(Currency.IDR);
        account.setAccountStatus(AccountStatus.OPEN);
        repository.save(account);
        return account;
    }

    @Override
    public String approveAccount(Long id) {
        Account accountAudit = repository.findById(id);
        Account account1 = cloneAccount(accountAudit);
        Account oldAccount = repository.findFirstActive(accountAudit.getNumber());

        if (!account1.getModified_by().equals(getLoggedInUser())) {
            account1.setApproved_by(getLoggedInUser());
            account1.setTimestamp(new Timestamp(System.currentTimeMillis()));
            if (account1.getStatus().equals(UserAndAccountStatus.UNDER_DELETE_APPROVAL)) {

                accountAudit.setStatus(UserAndAccountStatus.UNDER_DELETE_APPROVAL_AUDIT);
                repository.save(accountAudit);

                Account accountAudit1 = repository.findFirstActive(accountAudit.getNumber());
                accountAudit1.setStatus(UserAndAccountStatus.ACTIVE_AUDIT);
                repository.save(accountAudit1);

                account1.setStatus(UserAndAccountStatus.DELETED);
            } else if (account1.getStatus().equals(UserAndAccountStatus.UNDER_UPDATE_APPROVAL)) {

                accountAudit.setStatus(UserAndAccountStatus.UNDER_UPDATE_APPROVAL_AUDIT);
                repository.save(accountAudit);

                Account accountAudit1 = repository.findFirstActive(accountAudit.getNumber());
                accountAudit1.setStatus(UserAndAccountStatus.ACTIVE_AUDIT);
                repository.save(accountAudit1);

                account1.setStatus(UserAndAccountStatus.ACTIVE);

            } else {

                accountAudit.setStatus(UserAndAccountStatus.UNDER_ADD_APPROVAL_AUDIT);
                repository.save(accountAudit);

                account1.setStatus(UserAndAccountStatus.ACTIVE);

                Balance balance = new Balance();
                balance.setAccount(account1);
                balance.setAvailable("2000");
                balance.setCredit("2000");
                balance.setCountCredit("1");
                balance.setProjected("2000");
                balanceRepository.save(balance);
            }

            repository.save(account1);
            if (balanceRepository.existsByAccount(oldAccount)) {
                assignBalance(oldAccount, repository.findFirstActive(account1.getNumber()));
            }
            return "Success !";
        }
        return "You cannot approve this account !";
    }

    @Override
    public String getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public Account cloneAccount(Account account) {
        Account account1 = new Account();
        account1.setModified_by(account.getModified_by());
        account1.setCurrency(account.getCurrency());
        account1.setNumber(account.getNumber());
        account1.setAddress(account.getAddress());
        account1.setFullName(account.getFullName());
        account1.setTimestamp(new Timestamp(System.currentTimeMillis()));
        account1.setAccountStatus(account.getAccountStatus());
        account1.setStatus(account.getStatus());
        return account1;
    }

    @Override
    public Account updateAccount(Account account) {
        account.setStatus(UserAndAccountStatus.UNDER_UPDATE_APPROVAL);
        account.setModified_by(getLoggedInUser());
        return repository.save(account);
    }

    @Override
    public String deleteAccount(Long id) {
        Account account = repository.findById(id);
        account = cloneAccount(account);
        account.setModified_by(getLoggedInUser());
        account.setStatus(UserAndAccountStatus.UNDER_DELETE_APPROVAL);
        repository.save(account);
        return "Success !";
    }

    @Override
    public List<Account> findUnderApprovalAccounts() {
        return repository.findUnderApprovalAccounts();
    }

    @Override
    public List<Account> findAllByNumber(String number) {
        return repository.findAccountAudit(number);
    }

    @Override
    public void assignBalance(Account oldAccount, Account newAccount) {
        Balance balance = balanceRepository.findFirstByAccount_IdOrderByTimestampDesc(oldAccount.getId());
        balance.setAccount(newAccount);
        balanceRepository.save(balance);
        if (paymentRepository.existsByDebitAccount(oldAccount)) {
            List<Payment> paymentListDebit = paymentRepository.findAllActivePaymentsByDebitAccount_Id(oldAccount.getId());
            for (Payment payment : paymentListDebit) {
                if (payment.getDebitAccount().equals(oldAccount)) payment.setDebitAccount(newAccount);
                else payment.setCreditAccount(newAccount);
                paymentRepository.save(payment);
            }
        }
    }
}
