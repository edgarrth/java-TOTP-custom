package com.poc.totp.handler;


import com.poc.totp.domain.TotpRequest;
import com.poc.totp.util.SecretGenerator;
import com.poc.totp.util.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@RestController
@RequestMapping("/totp")
public class TotpController {
    private final TotpService totpService;

    public TotpController() {
        this.totpService = new TotpService();
    }

    @PostMapping("/enroll")
    public ResponseEntity<TotpRequest> enrollUser() {
        String base32Secret = SecretGenerator.generateSecret(32);
        return ResponseEntity.ok(new TotpRequest("", "", base32Secret));
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateTotp(@RequestBody TotpRequest request) {
        try {
            String otp = totpService.generateOtp(request.getBase32Secret(), Instant.now());
            return ResponseEntity.ok(otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateTotp(@RequestBody TotpRequest request) {
        try {
            boolean valid = totpService.validateOtp(request.getBase32Secret(), request.getOtp(), Instant.now(), 1);
            return ResponseEntity.ok(valid);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
