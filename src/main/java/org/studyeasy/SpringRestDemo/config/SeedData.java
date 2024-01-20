package org.studyeasy.SpringRestDemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.SpringRestDemo.util.constants.Authority;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private AccountService accountService;

    @Override
    public void run(final String... args) {
        final var account01 = new Account();
        final var account02 = new Account();

        account01.setEmail("user@user.com");
        account01.setPassword("pass987");
        account01.setAuthorities(Authority.USER.toString());
        accountService.save(account01);

        account02.setEmail("admin@admin.com");
        account02.setPassword("pass987");
        account02.setAuthorities(Authority.ADMIN + " " + Authority.USER);
        accountService.save(account02);
    }
}
