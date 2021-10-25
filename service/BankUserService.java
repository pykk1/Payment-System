package com.montran.paymentsystem.service;

import com.montran.paymentsystem.config.UserPrincipal;
import com.montran.paymentsystem.entity.BankUser;
import com.montran.paymentsystem.repository.BankUserRepository;
import com.montran.paymentsystem.status.UserAndAccountStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional
public class BankUserService implements BankUserServiceInterface, UserDetailsService {

    @Autowired
    private BankUserRepository repository;

    @Override
    public List<BankUser> showUnderApprovalUsers() {
        return repository.findUnderApprovalUsers();
    }

    @Override
    public List<BankUser> findFirstByStatus(String status) {
        return repository.findDistinctByStatus(status);
    }

    @Override
    public String addBankUser(BankUser bankUser) {
        if (repository.existsByUsername(bankUser.getUsername()))
            return "Username already exists !";
        bankUser.setStatus(UserAndAccountStatus.UNDER_ADD_APPROVAL);
        bankUser.setModified_by(getLoggedInUser());
        repository.save(bankUser);
        return "Success !";
    }

    @Override
    public String approveBankUser(Long id) {
        BankUser bankUserAudit = repository.findById(id);
        BankUser bankUser1 = cloneBankUser(bankUserAudit);
        if (!bankUser1.getModified_by().equals(getLoggedInUser())) {
            bankUser1.setApproved_by(getLoggedInUser());
            bankUser1.setTimestamp(new Timestamp(System.currentTimeMillis()));
            if (bankUser1.getStatus().equals(UserAndAccountStatus.UNDER_DELETE_APPROVAL)) {

                bankUserAudit.setStatus(UserAndAccountStatus.UNDER_DELETE_APPROVAL_AUDIT);
                repository.save(bankUserAudit);

                BankUser bankUserAudit1 = repository.findFirstActive(bankUserAudit.getUsername());
                bankUserAudit1.setStatus(UserAndAccountStatus.ACTIVE_AUDIT);
                repository.save(bankUserAudit1);

                bankUser1.setStatus(UserAndAccountStatus.DELETED);
            } else if (bankUser1.getStatus().equals(UserAndAccountStatus.UNDER_UPDATE_APPROVAL)) {

                bankUserAudit.setStatus(UserAndAccountStatus.UNDER_UPDATE_APPROVAL_AUDIT);
                repository.save(bankUserAudit);

                BankUser bankUserAudit1 = repository.findFirstActive(bankUserAudit.getUsername());
                bankUserAudit1.setStatus(UserAndAccountStatus.ACTIVE_AUDIT);
                repository.save(bankUserAudit1);

                bankUser1.setStatus(UserAndAccountStatus.ACTIVE);
            } else {

                bankUserAudit.setStatus(UserAndAccountStatus.UNDER_ADD_APPROVAL_AUDIT);
                repository.save(bankUserAudit);

                bankUser1.setStatus(UserAndAccountStatus.ACTIVE);
            }


            repository.save(bankUser1);
            return "Success !";
        }
        return "You cannot approve this user !";
    }

    @Override
    public String getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public String updateBankUser(BankUser bankUser) {
        BankUser bankUser1 = repository.findFirstByUsernameOrderByTimestampDesc(bankUser.getUsername());
        bankUser.clonePassword(bankUser1.getPassword());
        bankUser.setStatus("UNDER_UPDATE_APPROVAL");
        bankUser.setModified_by(getLoggedInUser());
        repository.save(bankUser);
        return "Success !";
    }

    @Override
    public BankUser cloneBankUser(BankUser bankUser) {
        BankUser bankUser1 = new BankUser();
        bankUser1.setModified_by(bankUser.getModified_by());
        bankUser1.setEmail(bankUser.getEmail());
        bankUser1.clonePassword(bankUser.getPassword());
        bankUser1.setAddress(bankUser.getAddress());
        bankUser1.setFullName(bankUser.getFullName());
        bankUser1.setUsername(bankUser.getUsername());
        bankUser1.setTimestamp(new Timestamp(System.currentTimeMillis()));
        bankUser1.setStatus(bankUser.getStatus());

        return bankUser1;
    }

    @Override
    public String deleteBankUser(Long id) {
        BankUser bankUser = repository.findById(id);
        bankUser = cloneBankUser(bankUser);
        bankUser.setModified_by(getLoggedInUser());
        bankUser.setStatus("UNDER_DELETE_APPROVAL"); //clasa constante private final static string
        repository.save(bankUser);
        return "Success !";
    }

    @Override
    public List<BankUser> findAllByUsername(String username) {
        return repository.findBankUserByUsernameOrderByTimestampDesc(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BankUser bankUser = repository.findFirstByUsernameOrderByTimestampDesc(username);
        if (bankUser == null) {
            throw new UsernameNotFoundException("User not found !");
        }
        return new UserPrincipal(bankUser);
    }

}
