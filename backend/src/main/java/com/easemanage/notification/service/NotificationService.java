package com.easemanage.notification.service;

import com.easemanage.common.dto.PagedResponse;
import com.easemanage.notification.dto.NotificationResponse;
import com.easemanage.notification.entity.Notification;
import com.easemanage.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public void createNotification(Long userId, String title, String message, String type) {
        notificationRepository.save(Notification.builder()
            .userId(userId).title(title).message(message).type(type).build());
        log.info("Notification created for userId={}, title={}, type={}", userId, title, type);
    }

    public void createForAllAdmins(String title, String message, String type, List<Long> adminIds) {
        for (Long adminId : adminIds) {
            createNotification(adminId, title, message, type);
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            notifications.getContent().stream().map(this::toResponse).toList(),
            notifications.getNumber(), notifications.getSize(),
            notifications.getTotalElements(), notifications.getTotalPages(), notifications.isLast()
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(),
            n.getType(), n.getIsRead(), n.getCreatedAt());
    }
}
