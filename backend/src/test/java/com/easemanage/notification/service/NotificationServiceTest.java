package com.easemanage.notification.service;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.notification.dto.NotificationResponse;
import com.easemanage.notification.entity.Notification;
import com.easemanage.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createNotification_saves() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createNotification(1L, "Test Title", "Test message", "INFO");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("Test Title", saved.getTitle());
        assertEquals("Test message", saved.getMessage());
        assertEquals("INFO", saved.getType());
    }

    @Test
    void getUnreadCount_returnsCorrectCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(1L);
    }

    @Test
    void markAsRead_updatesNotification() {
        Notification notification = Notification.builder()
                .id(1L).userId(1L).title("Alert").message("msg")
                .type("INFO").isRead(false).build();
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L);

        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAllAsRead_callsBulkUpdate() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsRead(1L);
    }

    @Test
    void getNotifications_returnsPagedResponse() {
        Notification notification = Notification.builder()
                .id(1L).userId(1L).title("Alert").message("Low stock")
                .type("LOW_STOCK").isRead(false).build();
        notification.setCreatedAt(LocalDateTime.now());

        Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1);
        when(notificationRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

        PagedResponse<NotificationResponse> result = notificationService.getNotifications(1L, 0, 10);

        assertEquals(1, result.content().size());
        assertEquals("Alert", result.content().get(0).title());
        assertEquals("LOW_STOCK", result.content().get(0).type());
        assertFalse(result.content().get(0).isRead());
    }

    @Test
    void createForAllAdmins_createsMultipleNotifications() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.createForAllAdmins("System Alert", "Message", "SYSTEM", List.of(1L, 2L, 3L));

        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void markAsRead_notFound_doesNothing() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        notificationService.markAsRead(99L);

        verify(notificationRepository, never()).save(any());
    }
}
