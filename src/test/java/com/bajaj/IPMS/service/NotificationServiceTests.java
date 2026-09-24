package com.bajaj.IPMS.service;

import com.bajaj.IPMS.model.Notification;
import com.bajaj.IPMS.model.User;
import com.bajaj.IPMS.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTests {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    UserService userService;

    @Mock
    NotificationRepository notificationRepository;

    @Test
    public void testGetNotifications() {
        User user = new User();
        user.setId(1L);

        Notification notification = new Notification();
        notification.setId(100L);
        notification.setMessage("Policy Cancelled");
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setUser(user);

        when(userService.getCurrUser()).thenReturn(user);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        ResponseEntity<?> response = notificationService.getNotifications();

        assertNotNull(response);
    }

    @Test
    public void testReadNotificationSuccess() {
        Notification notification = new Notification();
        notification.setId(200L);
        notification.setRead(false);

        when(notificationRepository.findById(200L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        ResponseEntity<?> response = notificationService.readNotification(200L);

        assertNotNull(response);
    }
}
