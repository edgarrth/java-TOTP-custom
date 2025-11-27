package com.poc.totp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.poc.totp.util.SecretGenerator;
import com.poc.totp.util.TotpService;

@SpringBootApplication
public class TotpApplication {

    public static void main(String[] args) {
        SpringApplication.run(TotpApplication.class, args);
    }

}
