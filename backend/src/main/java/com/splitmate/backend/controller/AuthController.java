package com.splitmate.backend.controller;

import com.splitmate.backend.model.User;
import com.splitmate.backend.repository.UserRepository;
import com.splitmate.backend.security.JwtUtil;
import com.splitmate.backend.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
        }

        String otp = otpService.sendOtp(phone);

        // For development: include OTP in response (remove in production)
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "phone", phone,
                "otp_dev", otp // DEV ONLY - remove for production
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String otp = body.get("otp");

        if (phone == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone and OTP are required"));
        }

        if (!otpService.verifyOtp(phone, otp)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired OTP"));
        }

        // Create or get user
        Optional<User> existingUser = userRepository.findByPhone(phone);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = User.builder()
                    .phone(phone)
                    .name("User_" + phone.substring(phone.length() - 4))
                    .build();
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "phone", user.getPhone(),
                "name", user.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (body.containsKey("name") && body.get("name") != null) {
                user.setName(body.get("name"));
            }

            if (body.containsKey("avatarUrl") && body.get("avatarUrl") != null) {
                user.setAvatarUrl(body.get("avatarUrl"));
            }

            if (body.containsKey("upiId") && body.get("upiId") != null) {
                user.setUpiId(body.get("upiId"));
                user.setUpiIdVerified(false); // Mark for verification
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "user", user));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }
}
