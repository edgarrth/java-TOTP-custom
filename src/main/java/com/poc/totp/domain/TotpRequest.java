package com.poc.totp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotpRequest {
    private String secret;
    private String otp;
    private String base32Secret; //Semilla
}
