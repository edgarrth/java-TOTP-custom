package com.poc.totp.util;

import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;


public class SecretGenerator {

    //Genera un array aleatorio de 32 bytes (256bits) ya que es el tamnaño natural de HMAC-SHA256 siguiendo el estándar RFC 6238
    //codifica la semilla en base32
    public static String generateSecret(int sizeBytes) {
        byte[] buffer = new byte[sizeBytes];
        new SecureRandom().nextBytes(buffer);
        return new Base32().encodeToString(buffer);
    }

}
