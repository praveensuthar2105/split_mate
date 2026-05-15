package com.splitmate.backend.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, String> lastOtpForDev = new ConcurrentHashMap<>(); // Dev testing
    
    public String sendOtp(String phone) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(phone, otp);
        lastOtpForDev.put(phone, otp); // Store for dev testing
        
        // DEV: Print to console for testing
        System.out.println("[OTP] Phone: " + phone + " → OTP: " + otp);
        
        // PROD: Integrate Twilio or MSG91 here
        // twilioService.sendSms(phone, "Your OTP is: " + otp);
        
        return otp;
    }
    
    public boolean verifyOtp(String phone, String otp) {
        String storedOtp = otpStore.get(phone);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStore.remove(phone); // Clear OTP after verification
            lastOtpForDev.remove(phone); // Clear dev cache
            return true;
        }
        return false;
    }
    
    // Dev-only endpoint to get the last OTP sent
    public String getLastOtp(String phone) {
        return lastOtpForDev.getOrDefault(phone, null);
    }
}
