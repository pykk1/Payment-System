package com.montran.paymentsystem.config;

import com.montran.paymentsystem.entity.BankUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private BankUser bankUser;

    public UserPrincipal(BankUser bankUser) {
        this.bankUser = bankUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return bankUser.getPassword();
    }

    @Override
    public String getUsername() {
        return bankUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return bankUser.getStatus().equals("ACTIVE")
                || bankUser.getStatus().equals("UNDER_UPDATE_APPROVAL")
                || bankUser.getStatus().equals("UNDER_DELETE_APPROVAL");
    }
}
