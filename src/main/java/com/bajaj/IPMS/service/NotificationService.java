package com.bajaj.IPMS.service;

import com.bajaj.IPMS.DTO.NotificationDTO;
import com.bajaj.IPMS.model.Notification;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    UserService userService;

    public ResponseEntity<?> getNotifications() {
        User user = userService.getCurrUser();

        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        for (Notification notification: notifications){
            notificationDTOs.add(new NotificationDTO(notification));
        }

        return ResponseEntity.ok(notificationDTOs);
    }

    public ResponseEntity<?> readNotification(Long notifId) {
        Notification notification = notificationRepository.findById(notifId)
                .orElseThrow(() -> new IllegalArgumentException("Notification Not Found"));

        notification.setRead(true);

        notificationRepository.save(notification);

        return ResponseEntity.ok(Map.of("message", "Notification isRead"));
    }
}
