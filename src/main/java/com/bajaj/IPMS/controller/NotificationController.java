package com.bajaj.IPMS.controller;

import com.bajaj.IPMS.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        return notificationService.getNotifications();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> readNotification(@PathVariable("id") Long notifId){
        return notificationService.readNotification(notifId);
    }
}
