package com.codegym.salesmanagement.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartRunner implements CommandLineRunner {
    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Override
    public void run(String... args) throws Exception {
        passwordEncryptor.encryptAndSavePasswords();
    }
}
