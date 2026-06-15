package mth.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import mth.models.Menus;
import mth.models.PasswordResetOtp;
import mth.models.Rolesmapping;
import mth.models.Users;
import mth.repository.MenusRepository;
import mth.repository.PasswordResetOtpRepository;
import mth.repository.RolesmappingRepository;
import mth.repository.UsersRepository;

@Service
public class UsersService {

    @Autowired
    UsersRepository UR;

    @Autowired
    MenusRepository MR;

    @Autowired
    RolesmappingRepository RMR;

    @Autowired
    JwtService JWT;

    public Object signup(Users U) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Users> existing = UR.findByEmail(U.getEmail());
            if (existing.isPresent()) {
                response.put("code", 501);
                response.put("message", "Email ID already registered");
            } else {
                if (U.getRole() <= 0) {
                    U.setRole(1);       // default role
                }
                U.setStatus(1);     // active
                UR.save(U);
                response.put("code", 200);
                response.put("message", "User account has been created.");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    public Object signin(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = data.get("username").toString();
            String password = data.get("password").toString();

            Optional<Users> userOpt = UR.findByEmailAndPassword(username, password);
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                response.put("code", 200);
                response.put("jwt", JWT.generateJWT(username, user.getRole()));
            } else {
                response.put("code", 404);
                response.put("message", "Invalid Credentials!");
            }
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    public Object uinfo(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> payload = JWT.validateJWT(token);
            String email = (String) payload.get("username");

            Optional<Users> userOpt = UR.findByEmail(email);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "User not found");
                return response;
            }
            Users U = userOpt.get();

            // Get menus for this user's role via Rolesmapping
            List<Rolesmapping> mappings = RMR.findByRole((long) U.getRole());
            List<Long> mids = mappings.stream().map(Rolesmapping::getMid).collect(Collectors.toList());
            List<Menus> menuList = MR.findAll().stream()
                    .filter(m -> mids.contains(m.getMid()))
                    .collect(Collectors.toList());

            response.put("code", 200);
            response.put("fullname", U.getFullname());
            response.put("email", U.getEmail());
            response.put("phone", U.getPhone());
            response.put("role", U.getRole());
            response.put("menulist", menuList);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @Autowired
    PasswordResetOtpRepository PROTP;

    @Autowired
    JavaMailSender mailSender;

    public Object getAllUsers() {
        return UR.findAll();
    }

    public Object requestForgotPasswordOtp(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (data.get("email") == null) {
                response.put("code", 400);
                response.put("message", "Email is required.");
                return response;
            }
            String email = data.get("email").toString();

            Optional<Users> userOpt = UR.findByEmail(email);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "Email ID is not registered.");
                return response;
            }

            // Generate 6-digit random numeric OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            Instant expiryTime = Instant.now().plus(5, ChronoUnit.MINUTES);

            // Clean up any old OTP for this email
            PROTP.deleteByEmail(email);

            // Save new OTP
            PasswordResetOtp resetOtp = new PasswordResetOtp(email, otp, expiryTime);
            PROTP.save(resetOtp);

            // Send Email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("reachsainikhil@gmail.com");
            message.setTo(email);
            message.setSubject("Password Reset OTP - Accessible Knowledge Accessing System");
            message.setText("Hello,\n\nYou have requested a password reset for your Accessible Knowledge Accessing System (AKS) account.\n\nYour One-Time Password (OTP) is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.\n\nBest regards,\nAKS Admin Team");
            
            mailSender.send(message);

            response.put("code", 200);
            response.put("message", "OTP has been sent to your email.");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Failed to send OTP: " + e.getMessage());
        }
        return response;
    }

    public Object resetPassword(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (data.get("email") == null || data.get("otp") == null || data.get("password") == null) {
                response.put("code", 400);
                response.put("message", "Email, OTP, and new password are required.");
                return response;
            }
            String email = data.get("email").toString();
            String otp = data.get("otp").toString();
            String newPassword = data.get("password").toString();

            Optional<PasswordResetOtp> otpOpt = PROTP.findByEmail(email);
            if (!otpOpt.isPresent()) {
                response.put("code", 400);
                response.put("message", "No OTP found or requested for this email.");
                return response;
            }

            PasswordResetOtp resetOtp = otpOpt.get();
            if (resetOtp.getExpiryTime().isBefore(Instant.now())) {
                PROTP.deleteByEmail(email);
                response.put("code", 400);
                response.put("message", "OTP has expired. Please request a new one.");
                return response;
            }

            if (!resetOtp.getOtp().equals(otp)) {
                response.put("code", 400);
                response.put("message", "Invalid OTP.");
                return response;
            }

            // OTP is valid! Reset user password
            Optional<Users> userOpt = UR.findByEmail(email);
            if (!userOpt.isPresent()) {
                response.put("code", 404);
                response.put("message", "User not found.");
                return response;
            }

            Users user = userOpt.get();
            user.setPassword(newPassword);
            UR.save(user);

            // Clean up the used OTP
            PROTP.deleteByEmail(email);

            response.put("code", 200);
            response.put("message", "Password has been reset successfully.");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
