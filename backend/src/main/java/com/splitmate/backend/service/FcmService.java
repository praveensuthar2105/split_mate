package com.splitmate.backend.service;

import org.springframework.stereotype.Service;

@Service
public class FcmService {

    // Mock implementation for sending FCM push notifications
    public void sendToGroup(String groupId, String title, String body) {
        System.out.println("====== FIREBASE PUSH NOTIFICATION ======");
        System.out.println("To Group: " + groupId);
        System.out.println("Title: " + title);
        System.out.println("Body: " + body);
        System.out.println("========================================");
    }
}