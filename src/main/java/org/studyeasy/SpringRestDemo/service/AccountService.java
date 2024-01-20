package org.studyeasy.SpringRestDemo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.repository.AccountRepository;
import org.studyeasy.SpringRestDemo.util.constants.Authority;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void save(final Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        if (account.getAuthorities() == null) {
            account.setAuthorities(Authority.USER.toString());
        }
        accountRepository.save(account);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Optional<Account> findByEmail(final String email) {
        return accountRepository.findByEmail(email);
    }

    public Optional<Account> findById(final Long id) {
        return accountRepository.findById(id);
    }

    public void deleteById(final Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final var optionalAccount = accountRepository.findByEmail(email);
        if (optionalAccount.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }
        final var account = optionalAccount.get();
        final var grantedAuthority = new ArrayList<GrantedAuthority>();
        grantedAuthority.add(new SimpleGrantedAuthority(account.getAuthorities()));
        return new User(account.getEmail(), account.getPassword(), grantedAuthority);
    }
}
