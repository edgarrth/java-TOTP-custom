package com.poc.totp.util;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotpService {

    private static final Logger logger = LoggerFactory.getLogger(TotpService.class);
    private final String algorithm;
    private final int digits;
    private final int timeStepSeconds;

    public TotpService() {
        this.algorithm = "HmacSHA256"; //Algoritmo de autenticación simétrica
        this.digits = 6;
        this.timeStepSeconds = 30;
    }

    public String generateOtp(String base32Secret, Instant instant) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] key = new Base32().decode(base32Secret);
        long counter = instant.getEpochSecond() / timeStepSeconds;  //Calcula el contador del tiempo (epoch seg desde 1970)
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array();  //Convierte el contador en bytes para entrada de HMAC

        //Recibe llave y prepara algoritmo HMAC-SHA256.
        SecretKeySpec signKey = new SecretKeySpec(key, algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(signKey);
        byte[] hash = mac.doFinal(data); //Aplica HMAC sobre el contador

        String otpString = getOtpString(hash);

        logger.info("OTP generado [{}] para counter [{}] (epoch [{}]) fecha: [{}]", otpString, counter, instant.getEpochSecond(), new Date());

        return otpString;
    }

    public boolean validateOtp(String base32Secret, String otpIngresado, Instant instant, int toleranceWindows)
            throws NoSuchAlgorithmException, InvalidKeyException {
        logger.info("Validando OTP para codigo: [{}] ", otpIngresado);

        for (int i = -toleranceWindows; i <= toleranceWindows; i++) {
            Instant instantToCheck = instant.plusSeconds(i * timeStepSeconds);
            String expectedOtp = generateOtp(base32Secret, instantToCheck);
            if (expectedOtp.equals(otpIngresado)) {
                return true;
            }
        }
        return false;
    }

    private String getOtpString(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F; //Tomamos el ultimo byte  y extraemos el offset dinamico de 4 bits

        //Extraemos un numero binario de 31 bits
        int binary =
                ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);

        int otp = binary % (int) Math.pow(10, digits);

        return String.format("%0" + digits + "d", otp);
    }

}
