package mth.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "password_reset_otps")
public class PasswordResetOtp {
    @Id
    private String id;
    private String email;
    private String otp;
    private Instant expiryTime;

    public PasswordResetOtp() {}

    public PasswordResetOtp(String email, String otp, Instant expiryTime) {
        this.email = email;
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public Instant getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Instant expiryTime) { this.expiryTime = expiryTime; }
}
